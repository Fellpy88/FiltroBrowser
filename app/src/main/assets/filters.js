(() => {
  if (window.__filtroBrowserInstalled) return;
  window.__filtroBrowserInstalled = true;
  let active = "none";
  const original = navigator.mediaDevices && navigator.mediaDevices.getUserMedia
    ? navigator.mediaDevices.getUserMedia.bind(navigator.mediaDevices) : null;
  const streams = new WeakMap();
  function overlay(video, canvas) {
    const ctx = canvas.getContext("2d");
    let raf = 0, stopped = false;
    const draw = () => {
      if (stopped) return;
      if (video.readyState >= 2) {
        const w = video.videoWidth || 640, h = video.videoHeight || 480;
        if (canvas.width !== w) canvas.width = w;
        if (canvas.height !== h) canvas.height = h;
        ctx.save(); ctx.scale(-1,1); ctx.drawImage(video,-w,0,w,h); ctx.restore();
        const sx=w/640, sy=h/480;
        ctx.save(); ctx.scale(sx,sy);
        if (active === "glasses") drawGlasses(ctx);
        if (active === "freckles") drawFreckles(ctx);
        if (active === "beard") drawBeard(ctx);
        if (active === "bald") drawBald(ctx);
        if (active === "no_brows") drawNoBrows(ctx);
        ctx.restore();
      }
      raf = requestAnimationFrame(draw);
    };
    draw();
    return () => { stopped=true; cancelAnimationFrame(raf); };
  }
  function drawGlasses(c) {
    c.strokeStyle="#171717"; c.lineWidth=7; c.shadowColor="#0008"; c.shadowBlur=5;
    c.beginPath(); c.roundRect(185,178,105,65,18); c.roundRect(350,178,105,65,18); c.moveTo(290,205); c.quadraticCurveTo(320,190,350,205); c.stroke();
    c.fillStyle="#8ac8e8"; c.globalAlpha=.16; c.fillRect(190,183,95,55); c.fillRect(355,183,95,55); c.globalAlpha=1; c.shadowBlur=0;
  }
  function drawFreckles(c) {
    c.fillStyle="#8a4b2b";
    for(let i=0;i<34;i++){let x=245+(i%9)*17+(i%2)*3,y=260+Math.floor(i/9)*13+(i%3)*2;c.beginPath();c.arc(x,y,1.8+(i%3)*.5,0,Math.PI*2);c.fill();}
  }
  function drawBeard(c) {
    c.fillStyle="rgba(42,28,22,.82)"; c.beginPath(); c.moveTo(205,300); c.quadraticCurveTo(220,390,320,420); c.quadraticCurveTo(420,390,435,300); c.quadraticCurveTo(390,350,320,355); c.quadraticCurveTo(250,350,205,300); c.fill();
    c.fillStyle="rgba(25,18,16,.9)"; c.beginPath(); c.ellipse(320,310,35,14,0,0,Math.PI*2); c.fill();
  }
  function drawBald(c) {
    c.fillStyle="rgba(205,166,139,.96)"; c.beginPath(); c.ellipse(320,112,120,70,0,Math.PI,Math.PI*2); c.fill();
    c.strokeStyle="rgba(120,80,65,.2)"; c.lineWidth=3; c.beginPath(); c.ellipse(320,112,120,70,0,Math.PI,Math.PI*2); c.stroke();
  }
  function drawNoBrows(c) {
    c.fillStyle="rgba(205,166,139,.98)"; c.fillRect(218,190,78,18); c.fillRect(345,190,78,18);
  }
  if (original) navigator.mediaDevices.getUserMedia = async function(constraints) {
    if (!constraints || !constraints.video) return original(constraints);
    const real = await original(constraints);
    const track = real.getVideoTracks()[0];
    if (!track) return real;
    const video = document.createElement("video");
    video.muted=true; video.playsInline=true; video.srcObject=real;
    await video.play();
    const canvas=document.createElement("canvas"); canvas.width=640; canvas.height=480;
    const stop=overlay(video,canvas);
    const filtered=canvas.captureStream(24);
    const audio=real.getAudioTracks();
    audio.forEach(t=>filtered.addTrack(t));
    const vt=filtered.getVideoTracks()[0];
    const oldStop=vt.stop.bind(vt);
    vt.stop=()=>{stop(); real.getTracks().forEach(t=>t.stop()); oldStop();};
    streams.set(filtered,real);
    return filtered;
  };
  window.FiltroBrowser = { setFilter(name){active=name;}, getFilter(){return active;} };
})();