/* Route PH - web page. All data (routes, accounts, comments) comes from the Java program: RoutePHServer.java */
const ORIGIN = "Polytechnic University of the Philippines, 1016 Anonas Street, Sta. Mesa, Manila";
const VEHICLE_SVG = '<svg viewBox="0 0 24 24"><path d="M4 16c0 .9.4 1.7 1 2.2V20a1 1 0 0 0 1 1h1a1 1 0 0 0 1-1v-1h8v1a1 1 0 0 0 1 1h1a1 1 0 0 0 1-1v-1.8c.6-.5 1-1.3 1-2.2V6c0-3.5-3.6-4-8-4S4 2.5 4 6v10zm3.5 1a1.5 1.5 0 1 1 0-3 1.5 1.5 0 0 1 0 3zm9 0a1.5 1.5 0 1 1 0-3 1.5 1.5 0 0 1 0 3zM6 11V6h12v5H6z"/></svg>';

/* ---------- GOOGLE MAPS ---------- */
const ORIGIN_Q = "PUP Main Campus, Sta. Mesa, Manila";      // name shown on screen
const ORIGIN_MAP = "14.59722,121.01083";                  // exact spot of PUP Main Campus (Anonas St.) used by the map
const DEST_Q = { Caloocan:"Caloocan City, Philippines", Bulacan:"Olympic Avenue, Deca Homes Subdivision, Loma de Gato, Marilao, Bulacan" };
const enc = encodeURIComponent;
const embedUrl = d => `https://maps.google.com/maps?saddr=${ORIGIN_MAP}&daddr=${enc(DEST_Q[d])}&dirflg=r&output=embed`;
const openUrl  = d => `https://www.google.com/maps/dir/?api=1&origin=${ORIGIN_MAP}&destination=${enc(DEST_Q[d])}&travelmode=transit`;

/* live tracking + map that follows the CURRENT step */
let watchId = null, lastMapUpdate = 0, lastPos = null;
const dirMode = v => v==="Walk" ? "w" : (v==="Tricycle"||v==="E-Bike") ? "d" : "r";   // walk / drive / transit

let mapView = "step";
function updateStepMap(reload=true){
  const frame = $("#trackFrame"), msg = $("#trackMsg");
  const steps = state.routes[state.route].steps;
  const step = steps[state.navIdx];
  if(!step) return;
  const [title, , vehicle, place] = step;
  const from = state.navIdx>0 ? steps[state.navIdx-1][3] : ORIGIN_MAP;       // start of this step
  const fromLabel = state.navIdx>0 ? from : ORIGIN_Q;
  const me = lastPos ? `📍 You: ${lastPos.la.toFixed(5)}, ${lastPos.lo.toFixed(5)}` : "";

  if(mapView==="me"){
    if(!lastPos){ msg.textContent = "Waiting for your location… (allow location in Chrome)"; return; }
    if(reload) frame.src = `https://maps.google.com/maps?q=${lastPos.la},${lastPos.lo}&z=17&output=embed`;
    msg.textContent = me;
  }else if(mapView==="whole"){
    const stops = [];
    steps.forEach(x=>{ if(stops[stops.length-1]!==x[3]) stops.push(x[3]); });
    if(reload) frame.src = `https://maps.google.com/maps?saddr=${ORIGIN_MAP}&daddr=${stops.map(enc).join("+to:")}&dirflg=d&output=embed`;
    msg.textContent = "Whole route: PUP Main Campus → " + stops.join(" → ");
  }else{
    const mode = vehicle==="Walk" ? "w" : "d";          // walking line / road line (the line is drawn along roads)
    if(reload) frame.src = `https://maps.google.com/maps?saddr=${enc(from)}&daddr=${enc(place)}&dirflg=${mode}&output=embed`;
    msg.textContent = `Step ${state.navIdx+1}: ${title}\n${fromLabel} → ${place}   ${me}`;
  }
}
document.querySelectorAll(".mtab").forEach(b=>b.addEventListener("click",()=>{
  mapView = b.dataset.view;
  document.querySelectorAll(".mtab").forEach(x=>x.classList.toggle("active", x===b));
  lastMapUpdate = 0; updateStepMap();
}));
function startTracking(){
  $("#trackBox").hidden = false;
  lastMapUpdate = 0; lastPos = null;
  if(!navigator.geolocation){ $("#trackMsg").textContent = "Location not supported. Showing the step location only."; return; }
  watchId = navigator.geolocation.watchPosition(pos=>{
    lastPos = {la:pos.coords.latitude, lo:pos.coords.longitude};
    const now = Date.now();
    if(lastMapUpdate && now-lastMapUpdate < 10000) return;      // refresh about every 10 seconds
    lastMapUpdate = now;
    updateStepMap(mapView==='me');          // only reload the map when showing My location
  }, ()=>{ $("#trackMsg").textContent = "Location blocked (allow it with the lock icon in Chrome). Showing the step location only."; },
  { enableHighAccuracy:true, maximumAge:5000 });
}
function stopTracking(){
  if(watchId!==null){ navigator.geolocation.clearWatch(watchId); watchId=null; }
  lastPos = null; $("#trackBox").hidden = true; $("#trackFrame").removeAttribute("src");
}

/* ---------- HELPERS ---------- */
const $ = s => document.querySelector(s);
const esc = t => String(t).replace(/[&<>"]/g, c=>({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;"}[c]));
const form = o => new URLSearchParams(o);

// talk to the Java server
async function api(path, opts){
  let res;
  try{ res = await fetch(path, opts); }
  catch(e){ throw new Error("Cannot reach the Java program. Run RoutePHServer, then open http://localhost:8080"); }
  let data = {};
  try{ data = await res.json(); }catch(e){ throw new Error("Cannot reach the Java program. Open http://localhost:8080"); }
  if(!res.ok) throw new Error(data.error || "Something went wrong.");
  return data;
}
const post = (path, obj) => api(path, {method:"POST", body:form(obj)});

const getSession = () => { try{ return localStorage.getItem("rph_session"); }catch(e){ return null; } };
const setSession = u => { try{ u ? localStorage.setItem("rph_session",u) : localStorage.removeItem("rph_session"); }catch(e){} };

/* ---------- STATE ---------- */
const state = { user:getSession(), dest:null, route:null, navIdx:-1, routes:[], mode:"login" };
const routeKey = () => state.dest + "|" + state.routes[state.route].name;

/* ---------- SCREENS ---------- */
function show(id){
  document.querySelectorAll(".screen").forEach(s=>s.classList.toggle("active", s.id===id));
  $("#nav").hidden = (id==="splash" || id==="auth");
  $("#navHome").classList.toggle("active", id!=="notifs");
  $("#navNotif").classList.toggle("active", id==="notifs");
  updateBadge();
}
function enterApp(){ $("#userName").textContent = state.user; show("home"); }
setTimeout(()=> state.user ? enterApp() : show("auth"), 2200);

/* ---------- LOGIN / CREATE ACCOUNT ---------- */
function setMode(m){
  state.mode = m;
  $("#tabLogin").classList.toggle("active", m==="login");
  $("#tabCreate").classList.toggle("active", m==="create");
  $("#confirmField").hidden = m!=="create";
  $("#authSubmit").textContent = m==="login" ? "log in" : "create account";
  $("#authHint").textContent = m==="login" ? 'No account yet? Tap "Create account".' : "Choose a username and password.";
  $("#authError").hidden = true;
}
$("#tabLogin").onclick = ()=>setMode("login");
$("#tabCreate").onclick = ()=>setMode("create");

$("#authForm").addEventListener("submit", async e=>{
  e.preventDefault();
  const user = $("#authUser").value.trim(), pass = $("#authPass").value;
  const err = $("#authError");
  const fail = m => { err.textContent=m; err.hidden=false; };
  if(!user || !pass) return fail("Please enter your username and password.");
  try{
    if(state.mode==="create" && pass!==$("#authPass2").value) return fail("Passwords do not match.");
    const d = await post(state.mode==="create" ? "/api/register" : "/api/login", {user, pass});
    state.user = d.user; setSession(d.user);
    $("#authForm").reset(); err.hidden = true;
    enterApp();
  }catch(x){ fail(x.message); }
});

$("#logoutBtn").onclick = ()=>{
  state.user=null; setSession(null);
  closeSheet(); setMode("login"); show("auth");
};

/* ---------- HOME: search destination ---------- */
$("#searchForm").addEventListener("submit", async e=>{
  e.preventDefault();
  const err = $("#formError");
  try{
    const d = await api("/api/routes?dest="+encodeURIComponent($("#toInput").value.trim()));
    err.hidden = true;
    state.dest = d.dest; state.routes = d.routes;
    renderRoutes(); show("routes");
  }catch(x){ err.textContent = x.message; err.hidden = false; }
});

/* ---------- ROUTES ---------- */
function renderRoutes(){
  $("#destTitle").textContent = "To " + state.dest;
  $("#mapFrame").src = embedUrl(state.dest);
  $("#openMaps").href = openUrl(state.dest);
  const list = $("#routeList");
  list.innerHTML = "";
  state.routes.forEach((r,i)=>{
    const parts = r.time.split(" ");
    const big = parts.length>2 ? parts[0]+" <small>hr</small>" : parts[0]+" <small>"+parts[1]+"</small>";
    const rest = parts.length>2 ? parts[2]+" <small>mins</small>" : "";
    const el = document.createElement("div");
    el.className = "card";
    el.innerHTML = `
      <div class="tag">${esc(r.tag)}</div>
      <div class="top"><span class="ico">${VEHICLE_SVG}</span><h4>${esc(r.title)}</h4>
        <span class="time">${big}${rest?"<br>"+rest:""}</span></div>
      <div class="sum">${esc(r.path)}</div>
      <div class="foot"><span>${esc(r.note)}</span><a>View Steps →</a></div>`;
    el.addEventListener("click",()=>openSheet(i));
    list.appendChild(el);
  });
}
$("#backHome").onclick = ()=>show("home");
$("#navHome").onclick = ()=>show("home");
$("#navNotif").onclick = ()=>{ renderNotifs(); show("notifs"); };

/* ---------- STEPS SHEET ---------- */
function openSheet(i){
  stopTracking();
  state.route=i; state.navIdx=-1;
  const r = state.routes[i];
  $("#sheetIcon").innerHTML = VEHICLE_SVG;
  $("#sheetTitle").textContent = r.title;
  $("#sheetDur").textContent = r.time;
  $("#startNav").textContent = "➤ START NAVIGATION";
  $("#commentInput").value=""; $("#commentMsg").textContent="";
  $("#stepList").innerHTML = r.steps.map(([t,d])=>`<li><b>${esc(t)}</b><span>${esc(d)}</span></li>`).join("");
  renderComments();
  $("#sheet").hidden = false;
}
function closeSheet(){ $("#sheet").hidden = true; stopTracking(); }
$("#closeSheet").onclick = closeSheet;
$("#sheet").addEventListener("click",e=>{ if(e.target.id==="sheet") closeSheet(); });

$("#startNav").onclick = ()=>{
  const lis=[...document.querySelectorAll("#stepList li")];
  if(state.navIdx===-1) startTracking();
  state.navIdx++;
  if(state.navIdx>=lis.length){
    stopTracking();
    lis.forEach(l=>l.classList.remove("current","done"));
    state.navIdx=-1; $("#startNav").textContent="🎉 ARRIVED — START AGAIN"; return;
  }
  lis.forEach((l,k)=>{ l.classList.toggle("done",k<state.navIdx); l.classList.toggle("current",k===state.navIdx); });
  lis[state.navIdx].scrollIntoView({block:"center",behavior:"smooth"});
  updateStepMap();
  $("#startNav").textContent = state.navIdx===lis.length-1 ? "✔ FINISH" : "➤ NEXT STEP";
};

/* ---------- COMMENTS (each one shows the username) ---------- */
async function getComments(){
  try{ return (await api("/api/comments?key="+encodeURIComponent(routeKey()))).comments; }catch(e){ return []; }
}
async function renderComments(){
  const list = await getComments();
  const box = $("#commentList");
  if(!list.length){ box.innerHTML = '<p class="msg">No comments yet. Be the first!</p>'; return; }
  box.innerHTML = list.map(c=>`
    <div class="cm" data-id="${esc(c.id)}">
      <span class="who">@${esc(c.user)}</span><span class="when">${esc(c.at)}</span>
      <p>${esc(c.text)}</p>
      <button class="replybtn" type="button">Reply</button>
      ${c.replies.map(r=>`<div class="rep"><span class="who">@${esc(r.user)}</span><span class="when">${esc(r.at)}</span><p>${esc(r.text)}</p></div>`).join("")}
    </div>`).join("");
}
$("#postComment").onclick = async ()=>{
  const text = $("#commentInput").value.trim();
  try{
    const d = await post("/api/comments", {key:routeKey(), user:state.user, text});
    $("#commentMsg").textContent = d.message;
    if(text){ $("#commentInput").value=""; renderComments(); }
  }catch(x){ $("#commentMsg").textContent = x.message; }
};
$("#commentList").addEventListener("click", async e=>{
  const cm = e.target.closest(".cm"); if(!cm) return;
  if(e.target.classList.contains("replybtn")){
    if(cm.querySelector(".replyrow")) return;
    const row=document.createElement("div"); row.className="replyrow";
    row.innerHTML='<input type="text" placeholder="Write a reply…"><button type="button">Send</button>';
    cm.appendChild(row); row.querySelector("input").focus();
  }else if(e.target.closest(".replyrow button")){
    const text = cm.querySelector(".replyrow input").value.trim(); if(!text) return;
    try{ await post("/api/reply", {id:cm.dataset.id, user:state.user, text}); renderComments(); }
    catch(x){ $("#commentMsg").textContent = x.message; }
  }
});

/* ---------- NOTIFICATIONS (replies to YOUR comments) ---------- */
async function updateBadge(){
  if(!state.user) return;
  try{
    const d = await api("/api/notifications?user="+encodeURIComponent(state.user));
    const b=$("#badge"); b.hidden = !d.unread; b.textContent = d.unread;
  }catch(e){}
}
async function renderNotifs(){
  const el = $("#notifList");
  try{
    const d = await api("/api/notifications?read=1&user="+encodeURIComponent(state.user));
    el.innerHTML = d.items.length ? "" :
      '<p class="empty">No new notifications.<br>When someone replies to your route<br>comment, the reply will appear here.</p>';
    el.innerHTML += d.items.reverse().map(n=>`
      <div class="notif ${n.unread?"new":""}"><b>@${esc(n.by)} replied to your comment • ${esc(n.at)}</b>
      “${esc(n.text)}”<br><small>on: ${esc(n.on)}</small></div>`).join("");
    $("#badge").hidden = true;
  }catch(x){ el.innerHTML = '<p class="empty">'+esc(x.message)+'</p>'; }
}
