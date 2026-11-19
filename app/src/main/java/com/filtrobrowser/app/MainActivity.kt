package com.filtrobrowser.app

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature

class MainActivity : Activity() {
    private lateinit var web: WebView
    private lateinit var address: EditText
    private lateinit var status: TextView
    private var pendingPermission: PermissionRequest? = null
    private val permissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        val p = pendingPermission
        if (p != null) {
            val granted = it[Manifest.permission.CAMERA] == true && it[Manifest.permission.RECORD_AUDIO] == true
            if (granted) p.grant(p.resources) else p.deny()
            pendingPermission = null
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(0xFF101820.toInt()) }
        val bar = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(8,8,8,8) }
        address = EditText(this).apply {
            setSingleLine(true); hint = "Digite um endereço"; textSize = 14f
            setText("https://"); setTextColor(0xFFFFFFFF.toInt()); setHintTextColor(0xFFBBBBBB.toInt())
            setBackgroundColor(0xFF263746.toInt())
        }
        val go = Button(this).apply { text = "IR"; setOnClickListener { loadAddress() } }
        bar.addView(address, LinearLayout.LayoutParams(0, 48, 1f)); bar.addView(go)
        root.addView(bar)
        status = TextView(this).apply { text = "Filtro Browser • selecione um efeito"; setTextColor(-1); textSize=12f; setPadding(12,4,12,4) }
        root.addView(status)
        web = WebView(this)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.mediaPlaybackRequiresUserGesture = false
        web.settings.javaScriptCanOpenWindowsAutomatically = true
        web.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest) {
                val wanted = request.resources
                val needsCamera = wanted.any { it == PermissionRequest.RESOURCE_VIDEO_CAPTURE }
                val needsMic = wanted.any { it == PermissionRequest.RESOURCE_AUDIO_CAPTURE }
                val okCam = !needsCamera || ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                val okMic = !needsMic || ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                if (okCam && okMic) runOnUiThread { request.grant(wanted) }
                else { pendingPermission = request; permissions.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)) }
            }
            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                status.text = "Web: ${consoleMessage.message().take(90)}"
                return true
            }
        }
        web.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                address.setText(url)
                status.text = "Página carregada • filtros experimentais"
            }
        }
        if (WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)) {
            WebViewCompat.addDocumentStartJavaScript(web, assets.open("filters.js").bufferedReader().use { it.readText() }, setOf("*"))
        }
        root.addView(web, LinearLayout.LayoutParams(-1,0,1f))
        val filters = LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER; setPadding(4,4,4,4) }
        listOf("Normal","Óculos","Sardas","Barba","Careca","Sem sobrancelha").forEach { name ->
            filters.addView(Button(this).apply {
                text=name; textSize=10f; setPadding(4,4,4,4)
                setOnClickListener { web.evaluateJavascript("window.FiltroBrowser && window.FiltroBrowser.setFilter('${when(name) {
                    "Óculos" -> "glasses"; "Sardas" -> "freckles"; "Barba" -> "beard"; "Careca" -> "bald"; "Sem sobrancelha" -> "no_brows"; else -> "none"
                }}')", null); status.text = "Efeito selecionado: $name" }
            }, LinearLayout.LayoutParams(0,48,1f))
        }
        root.addView(filters)
        setContentView(root)
        web.loadUrl("https://www.google.com")
    }

    private fun loadAddress() {
        var u = address.text.toString().trim()
        if (!u.startsWith("http://") && !u.startsWith("https://")) u = "https://$u"
        web.loadUrl(u)
    }
    override fun onBackPressed() {
        if (::web.isInitialized && web.canGoBack()) web.goBack() else super.onBackPressed()
    }
}
