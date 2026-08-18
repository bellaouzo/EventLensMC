(function(){"use strict";function w(e){return`${(e/1e6).toFixed(2)} ms`}function x(e){return!e||e==="—"?"—":e.length>10?e.slice(0,8):e}function b(e){const s=e.lastIndexOf(".");return s>=0?e.substring(s+1):e}function ce(e){const s=Math.max(0,e);if(s<1e3)return`${Math.round(s)}ms`;const n=Math.floor(s/1e3),t=Math.floor(n/3600),i=Math.floor(n%3600/60),o=n%60;return t>0?`${t}h ${i}m ${o}s`:i>0?`${i}m ${o}s`:`${o}s`}function c(e){return e.replaceAll("&","&amp;").replaceAll("<","&lt;").replaceAll(">","&gt;").replaceAll('"',"&quot;")}function is(e){return e.toLocaleTimeString([],{hour:"2-digit",minute:"2-digit",second:"2-digit",hour12:!1})}function as(e){const s=new Date(e),n=is(s),t=String(s.getMilliseconds()).padStart(3,"0");return`${n}.${t}`}const os=()=>window.location.protocol.startsWith("http")&&window.location.port!=="";async function rs(){return(await fetch("/api/status")).json()}async function Ae(){return(await(await fetch("/api/sessions")).json()).sessions??[]}async function ke(){return(await(await fetch("/api/reports")).json()).reports??[]}async function R(e){const s=await fetch(`/api/sessions/${encodeURIComponent(e)}/report`);if(!s.ok)throw new Error("Session report unavailable");return s.json()}async function Oe(e){const s=await fetch(`/api/reports/${encodeURIComponent(e)}`);if(!s.ok)throw new Error("Report file unavailable");return s.json()}async function ls(){return(await fetch("/api/graph/events")).json()}function I(){return os()}function de(e){return JSON.parse(e)}function cs(e){if(typeof e.sessionId!="string")return null;const s={sessionId:e.sessionId,eventClassName:typeof e.eventClassName=="string"?e.eventClassName:"",state:typeof e.state=="string"?e.state:"ACTIVE",capturedEvents:typeof e.capturedEvents=="number"?e.capturedEvents:0,durationMillis:typeof e.durationMillis=="number"?e.durationMillis:0,startedAtMillis:typeof e.startedAtMillis=="number"?e.startedAtMillis:Date.now()};return e.dispatch&&typeof e.dispatch=="object"&&(s.dispatch=ds(e.dispatch)),s}function ds(e){const s=Array.isArray(e.listenerTimings)?e.listenerTimings.filter(t=>typeof t=="object"&&t!==null).map(ps):[],n=Array.isArray(e.listenerChain)?e.listenerChain.filter(t=>typeof t=="object"&&t!==null).map(us):void 0;return{sequence:typeof e.sequence=="number"?e.sequence:0,startedAtMillis:typeof e.startedAtMillis=="number"?e.startedAtMillis:0,durationNanos:typeof e.durationNanos=="number"?e.durationNanos:0,durationMillis:typeof e.durationMillis=="string"?e.durationMillis:"0ms",eventLensOverheadNanos:typeof e.eventLensOverheadNanos=="number"?e.eventLensOverheadNanos:0,eventClassName:typeof e.eventClassName=="string"?e.eventClassName:"",cancelledAtStart:e.cancelledAtStart===!0,cancelledAtEnd:e.cancelledAtEnd===!0,playerName:typeof e.playerName=="string"?e.playerName:null,worldName:typeof e.worldName=="string"?e.worldName:null,blockX:typeof e.blockX=="number"?e.blockX:null,blockY:typeof e.blockY=="number"?e.blockY:null,blockZ:typeof e.blockZ=="number"?e.blockZ:null,listenerChain:n,listenerTimings:s}}function us(e){return{registrationOrder:typeof e.registrationOrder=="number"?e.registrationOrder:void 0,pluginName:typeof e.pluginName=="string"?e.pluginName:"",listenerClassName:typeof e.listenerClassName=="string"?e.listenerClassName:"",methodName:typeof e.methodName=="string"?e.methodName:"",priority:typeof e.priority=="string"?e.priority:"NORMAL"}}function ps(e){return{invocationOrder:typeof e.invocationOrder=="number"?e.invocationOrder:0,pluginName:typeof e.pluginName=="string"?e.pluginName:"",listenerClassName:typeof e.listenerClassName=="string"?e.listenerClassName:"",methodName:typeof e.methodName=="string"?e.methodName:"",priority:typeof e.priority=="string"?e.priority:"NORMAL",durationNanos:typeof e.durationNanos=="number"?e.durationNanos:0,durationMillis:typeof e.durationMillis=="string"?e.durationMillis:typeof e.durationMillis=="number"?`${e.durationMillis}ms`:"0ms",exceedsSlowThreshold:e.exceedsSlowThreshold===!0,threwException:e.threwException===!0,exceptionType:typeof e.exceptionType=="string"?e.exceptionType:null}}function fs(e,s){const n=s.dispatch,t=e&&e.session.sessionId===s.sessionId?e:ms(s),i=[...t.dispatches];if(n){const r=i.findIndex(a=>a.sequence===n.sequence);r>=0?i[r]=vs(i[r],n):i.push(n)}const o=Math.max(s.capturedEvents,i.length);return{...t,session:{...t.session,sessionId:s.sessionId,eventClassName:s.eventClassName||t.session.eventClassName,state:s.state,capturedEvents:o,durationMillis:s.durationMillis,startedAtMillis:s.startedAtMillis},dispatches:i}}function vs(e,s){const n=s.listenerTimings.length>0?s.listenerTimings:e.listenerTimings,t=s.listenerChain&&s.listenerChain.length>0?s.listenerChain:e.listenerChain;return{...s,listenerTimings:n,listenerChain:t}}function ms(e){return{reportVersion:"live",redactionMode:"live",session:{sessionId:e.sessionId,eventClassName:e.eventClassName,state:e.state,ownerName:"",startedAtMillis:e.startedAtMillis,durationMillis:e.durationMillis,capturedEvents:e.capturedEvents,droppedEvents:0,sampledOutEvents:0,filters:""},warnings:[],dispatches:e.dispatch?[e.dispatch]:[]}}const hs=2e3;function gs(e){let s=!0,n=!1,t=null,i=null;const o=()=>{n=!0,i!==null&&(window.clearInterval(i),i=null)},r=()=>{!s||i!==null||(i=window.setInterval(()=>{e("poll",{})},hs))},a=()=>{!s||t||(t=new EventSource("/api/stream"),t.onopen=()=>{o()},t.addEventListener("connected",()=>{o()}),t.addEventListener("dispatch",l=>{ue(e,"dispatch",l.data)}),t.addEventListener("session-started",l=>{ue(e,"session-started",l.data)}),t.addEventListener("session-stopped",l=>{ue(e,"session-stopped",l.data)}),t.onerror=()=>{n=!1,t==null||t.close(),t=null,r(),s&&window.setTimeout(a,1e3)})};return a(),{stop:()=>{s=!1,n=!1,t==null||t.close(),t=null,i!==null&&window.clearInterval(i)},connected:()=>n}}function ue(e,s,n){try{e(s,JSON.parse(n))}catch{e(s,{})}}function He(e){return e==="ACTIVE"||e==="THROTTLED"}function U(e){switch(e){case"ACTIVE":return{label:"Active",shortLabel:"ACTIVE",tone:"active",isActive:!0};case"THROTTLED":return{label:"Throttled",shortLabel:"THROTTLED",tone:"warn",isActive:!0};case"STOPPED":return{label:"Stopped",shortLabel:"STOPPED",tone:"stopped",isActive:!1};case"FULL":return{label:"Full",shortLabel:"FULL",tone:"stopped",isActive:!1};case"EXPIRED":return{label:"Expired",shortLabel:"EXPIRED",tone:"stopped",isActive:!1};case"ABANDONED":return{label:"Abandoned",shortLabel:"ABANDONED",tone:"stopped",isActive:!1};default:return{label:e,shortLabel:e,tone:"stopped",isActive:!1}}}function H(e){const s=U(e);return s.tone!=="stopped"?"":`
    <div class="page-notice page-notice-stopped" role="status">
      <span class="page-notice-dot"></span>
      <span class="page-notice-label">${c(s.shortLabel)}</span>
      <span class="page-notice-text">${c("This trace has ended. You are viewing a frozen snapshot — data will not update.")}</span>
    </div>`}const qe=5;let q=!0,pe=!1,V=[],Pe="";const bs=[{id:"overview",label:"Overview"},{id:"timeline",label:"Timeline"},{id:"flame",label:"Flame graph"},{id:"events",label:"Event graph"},{id:"plugins",label:"Plugin graph"},{id:"compare",label:"Compare"}];function $s(e,s){var n,t,i,o,r;e.className="app-shell",e.innerHTML=`
    <header class="app-header">
      <div class="brand">
        <div class="brand-row">
          <span class="brand-name">EventLens</span>
          <span class="brand-version" id="brand-version">v${c(s.eventLensVersion)}</span>
        </div>
        <span class="brand-sub">diagnostics — observer only</span>
      </div>
      <nav class="top-nav">
        ${bs.map(a=>`
          <button type="button" class="nav-item${s.activeView===a.id?" active":""}" data-view="${a.id}">
            ${a.label}
          </button>`).join("")}
      </nav>
      <div class="header-session" id="header-session">${Ge(s)}</div>
    </header>
    <div class="app-body">
      <aside class="sidebar">
        <div class="mode-toggle">
          <button type="button" class="mode-btn${s.sourceMode==="live"?" active":""}" data-mode="live"${s.liveAvailable?"":" disabled"}>Live</button>
          <button type="button" class="mode-btn${s.sourceMode==="offline"?" active":""}" data-mode="offline">Offline</button>
        </div>
        <section class="sidebar-section">
          <div class="sidebar-heading">Live sessions</div>
          <div id="stream-status">${De(s.streamConnected,s.liveAvailable)}</div>
          <div id="session-list" class="session-list"></div>
        </section>
        <section class="sidebar-section">
          <button type="button" id="reports-toggle" class="sidebar-disclosure collapsed">
            <span>Saved reports</span>
            <span id="reports-count" class="disclosure-count">0</span>
            <span class="disclosure-chevron" aria-hidden="true"></span>
          </button>
          <div id="reports-body" class="disclosure-body is-collapsed">
            <div id="report-list" class="report-list"></div>
            <button type="button" id="reports-more" class="sidebar-more" hidden></button>
            <label class="file-upload">
              <input type="file" id="file-input" accept=".json,application/json" />
              Load JSON
            </label>
          </div>
        </section>
        <section class="sidebar-section context-section">
          <div class="sidebar-heading">Context</div>
          <div id="context-list" class="context-list">${Fe(s)}</div>
        </section>
      </aside>
      <main id="view-root" class="view-root"></main>
    </div>
  `,e.querySelectorAll(".nav-item[data-view]").forEach(a=>{a.addEventListener("click",()=>{s.onNavigate(a.dataset.view)})}),e.querySelectorAll(".mode-btn[data-mode]").forEach(a=>{a.addEventListener("click",()=>{a.disabled||s.onSourceModeChange(a.dataset.mode)})}),(n=e.querySelector("#session-list"))==null||n.addEventListener("click",a=>{const l=a.target.closest("[data-session-id]");l!=null&&l.dataset.sessionId&&s.onSessionChange(l.dataset.sessionId)}),(t=e.querySelector("#report-list"))==null||t.addEventListener("click",a=>{const l=a.target.closest("[data-report]");l!=null&&l.dataset.report&&s.onReportChange(l.dataset.report)}),(i=e.querySelector("#reports-toggle"))==null||i.addEventListener("click",()=>{q=!q,Ve(e)}),(o=e.querySelector("#reports-more"))==null||o.addEventListener("click",()=>{pe=!0,Re(e,V,Pe)}),(r=e.querySelector("#file-input"))==null||r.addEventListener("change",a=>{var p;const u=(p=a.target.files)==null?void 0:p[0];u&&s.onFileLoad(u)})}function fe(e,s,n,t,i){const o=e.querySelector(`[data-session-id="${Ts(s)}"]`);if(!o)return;if(i){o.dataset.state=i;const a=o.querySelector(".session-badge");if(a){const l=U(i);a.textContent=l.shortLabel,a.className=`session-badge badge-${l.tone}`}}const r=o.querySelector(".session-card-sub");r&&(r.textContent=`${b(t)} — ${n} captured`)}function _(e,s,n){e.querySelectorAll("[data-session-id]").forEach(t=>{t.classList.toggle("selected",t.dataset.sessionId===s)}),e.querySelectorAll("[data-report]").forEach(t=>{t.classList.toggle("selected",!!n&&t.dataset.report===n)})}function xe(e,s,n){const t=e.querySelector("#stream-status");t&&(t.innerHTML=De(s,n))}function ys(e,s,n,t,i){const o=e.querySelector("#session-list");e.querySelector("#report-list"),o&&(o.innerHTML=s.length?s.map(a=>Ns(a,a.sessionId===t&&!i)).join(""):'<p class="sidebar-empty">No live sessions</p>'),V=n.map(a=>a.fileName),Pe=i;const r=e.querySelector("#reports-count");r&&(r.textContent=String(n.length)),Re(e,V,i),Ve(e)}function Re(e,s,n){const t=e.querySelector("#report-list"),i=e.querySelector("#reports-more");if(!t)return;const o=pe?s:s.slice(0,qe);if(t.innerHTML=o.length?o.map(r=>Ss(r,r===n)).join(""):'<p class="sidebar-empty">No saved reports</p>',i){const r=s.length-o.length;i.hidden=q||r<=0,i.textContent=r>0?`Show ${r} more`:""}}function Ve(e){const s=e.querySelector("#reports-body"),n=e.querySelector("#reports-toggle");s&&s.classList.toggle("is-collapsed",q),n==null||n.classList.toggle("collapsed",q);const t=e.querySelector("#reports-more");if(t&&q)t.hidden=!0;else if(t&&!q){const i=V.length-(pe?V.length:Math.min(qe,V.length));t.hidden=i<=0}}function je(e,s){e.querySelectorAll(".mode-btn[data-mode]").forEach(n=>{n.classList.toggle("active",n.dataset.mode===s)})}function C(e,s){const n=e.querySelector("#header-session");n&&(n.innerHTML=Ge(s));const t=e.querySelector("#brand-version");t&&(t.textContent=`v${s.eventLensVersion}`);const i=e.querySelector("#context-list");i&&(i.innerHTML=Fe(s)),xe(e,s.streamConnected,s.liveAvailable),je(e,s.sourceMode),_(e,s.selectedSessionId,s.selectedReportFile)}function Ge(e){const s=Ls(e),n=e.sessionState;if(!s||s==="—")return'<span class="header-session-id">—</span>';const t=n?U(n):null,i=t?`<span class="header-state-badge badge-${t.tone}">${c(t.shortLabel)}</span>`:"";return`<span class="header-session-id">${c(x(s))}</span>${i}`}function De(e,s){return s?`<div class="stream-status${e?" connected":""}"><span class="stream-dot${e?"":" off"}"></span>${e?"stream connected":"stream disconnected"}</div>`:'<div class="stream-status"><span class="stream-dot off"></span>offline viewer</div>'}function Ns(e,s){const n=U(e.state);return`
    <button type="button" class="session-card${s?" selected":""}" data-session-id="${ve(e.sessionId)}" data-state="${ve(e.state)}">
      <div class="session-card-top">
        <span class="session-id">${c(x(e.sessionId))}</span>
        <span class="session-badge badge-${n.tone}">${c(n.shortLabel)}</span>
      </div>
      <div class="session-card-sub">${c(b(e.eventClassName))} — ${e.capturedEvents} captured</div>
    </button>`}function Ss(e,s){return`<button type="button" class="report-item${s?" selected":""}" data-report="${ve(e)}">${c(e)}</button>`}function Fe(e){var h,X,ns;const s=e.report,n=e.serverStatus,t=s==null?void 0:s.session,i=e.dataSource==="session"?"live session":e.dataSource==="report"?"saved report":"offline file",o=(t==null?void 0:t.state)??e.sessionState??"—",r=o!=="—"?U(o):null,a=t?b(t.eventClassName):"—",l=((h=s==null?void 0:s.dispatches.find(Ce=>Ce.worldName))==null?void 0:h.worldName)??(n==null?void 0:n.defaultWorldName)??"—",u=(n==null?void 0:n.tps)??((X=s==null?void 0:s.dispatches.find(Ce=>Ce.tps!=null))==null?void 0:X.tps)??null,p=u!=null?u.toFixed(1):"—",v=u==null?"":u>=18?"tone-ok":"tone-warn",E=e.agentPresent?"present":"absent",k=e.protocolVersion>0?`v${e.protocolVersion}`:"—",f=((ns=s==null?void 0:s.instrumentation)==null?void 0:ns.mode)??(e.agentPresent?"precise":"dispatch");return`
    <div class="context-group">
      <div class="context-group-title">Session</div>
      <div class="context-chips">
        <span class="ctx-chip">${c(i)}</span>
        ${r?`<span class="ctx-chip chip-${r.tone}">${c(r.shortLabel)}</span>`:""}
        <span class="ctx-chip chip-event">${c(a)}</span>
      </div>
      ${T("ID",x((t==null?void 0:t.sessionId)??(e.selectedSessionId||"—")))}
      ${t?`<div class="context-row"><span>Captured</span><span class="context-counts"><b>${t.capturedEvents}</b><i>${t.droppedEvents}</i><em>${t.sampledOutEvents}</em></span></div>`:T("Captured","—")}
      ${T("Owner",(t==null?void 0:t.ownerName)||"—")}
      ${T("Filters",(t==null?void 0:t.filters)||"—")}
    </div>
    <div class="context-group">
      <div class="context-group-title">Server</div>
      ${T("World",l)}
      ${T("Game mode",(n==null?void 0:n.defaultGameMode)||"—")}
      ${T("Players",n?String(n.onlinePlayers):"—")}
      <div class="context-row"><span>TPS</span><span class="${v}">${c(p)}</span></div>
      ${T("Tick budget","50 ms")}
      ${T("Runtime",e.paperVersion)}
    </div>
    <div class="context-group">
      <div class="context-group-title">Instrumentation</div>
      ${T("EventLens",`v${e.eventLensVersion}`)}
      <div class="context-row"><span>Agent</span><span class="${e.agentPresent?"tone-ok":"tone-muted"}">${c(E)}</span></div>
      ${T("Protocol",k)}
      <div class="context-row"><span>Mode</span><span class="tone-accent">${c(f)}</span></div>
      ${T("Redaction",(s==null?void 0:s.redactionMode)||"—")}
    </div>`}function T(e,s){return`<div class="context-row"><span>${c(e)}</span><span>${c(s)}</span></div>`}function Ls(e){var s,n;return((s=e.report)==null?void 0:s.session.sessionId)??e.selectedSessionId??((n=e.serverStatus)==null?void 0:n.activeTraceSessionId)??"—"}function ve(e){return c(e).replaceAll("'","&#39;")}function Ts(e){return e.replaceAll("\\","\\\\").replaceAll('"','\\"')}function B(e){var i;if((((i=e.listenerTimings)==null?void 0:i.length)??0)>0)return e.listenerTimings;const s=e.listenerChain??[];if(s.length===0)return[];const n=Math.max(e.durationNanos,s.length),t=Math.floor(n/s.length);return s.map((o,r)=>{const a=r===s.length-1?n-t*(s.length-1):t;return{invocationOrder:o.registrationOrder??r+1,pluginName:o.pluginName,listenerClassName:o.listenerClassName,methodName:o.methodName,priority:o.priority,durationNanos:a,durationMillis:w(a),exceedsSlowThreshold:!1,threwException:!1,exceptionType:null}})}const Z=5e7;function z(e,s){const n=s.filter(i=>i.eventClassName===e.eventClassName);if(n.length<2)return!1;const t=Math.min(...n.map(i=>i.sequence));return e.sequence===t}function We(e,s){if(!e.length)return null;const n=e.filter(i=>!z(i,e));return[...n.length?n:e].sort(s)[0]}function me(e){var l;let s=0,n=0;const t=new Map;for(const u of e.dispatches){const p=z(u,e.dispatches);for(const v of B(u)){if(s+=1,p)continue;v.exceedsSlowThreshold&&(n+=1);const E=`${v.pluginName}::${b(u.eventClassName)}`,k=t.get(E),f=v.durationNanos/Z*100,h=ws(v,f);(!k||v.durationNanos>k.timeNanos)&&t.set(E,{plugin:v.pluginName,event:b(u.eventClassName),timeNanos:v.durationNanos,tickPercent:f,status:h,sequence:u.sequence})}}const i=[...t.values()].sort((u,p)=>p.timeNanos-u.timeNanos).slice(0,8),o=[...e.dispatches].sort((u,p)=>p.sequence-u.sequence).map(u=>Is(u,e.dispatches)),r=e.session.capturedEvents,a=((l=e.dispatches.find(u=>u.worldName))==null?void 0:l.worldName)??null;return{uptimeMillis:_e(e.session),eventsTraced:r,listenersInvoked:s,avgListenersPerEvent:r>0?s/r:0,flaggedListeners:n,topOffenders:i,recentTraces:o,primaryWorld:a}}function ws(e,s){return e.exceedsSlowThreshold||s>=10?"critical":s>=3?"warn":"ok"}function Is(e,s){if(z(e,s))return{sequence:e.sequence,label:b(e.eventClassName),detail:e.blockMaterial??e.playerName??null,startedAtMillis:e.startedAtMillis,durationNanos:e.durationNanos,severity:"warmup"};const n=[...e.listenerTimings??[]].sort((o,r)=>r.durationNanos-o.durationNanos)[0],t=e.durationNanos/Z*100;let i="ok";return t>=15||n!=null&&n.exceedsSlowThreshold?i="critical":t>=5&&(i="warn"),{sequence:e.sequence,label:b(e.eventClassName),detail:e.blockMaterial??e.playerName??null,startedAtMillis:e.startedAtMillis,durationNanos:e.durationNanos,severity:i}}function he(e){return e==="critical"?"CRITICAL":e==="warn"?"WARN":e==="warmup"?"WARMUP":"OK"}function Ue(e){return`${(e/Z*100).toFixed(1)}%`}function _e(e,s=Date.now()){return e.state==="ACTIVE"||e.state==="THROTTLED"?Math.max(0,s-e.startedAtMillis):e.durationMillis}const Q=8;let A=0;function ge(){A=0}function Be(e){return e==="critical"?"offender-tick-crit":e==="warn"?"offender-tick-warn":e==="warmup"?"offender-tick-warmup":"offender-tick-ok"}function be(e,s,n){var k,f;const t=me(s),i=t.flaggedListeners,o=t.topOffenders.length>0?t.topOffenders.map(h=>`
          <button type="button" class="offender-row row-clickable" data-sequence="${h.sequence}">
            <div class="offender-plugin">${c(h.plugin)}</div>
            <div class="offender-event">${c(h.event)}</div>
            <div class="offender-time">${w(h.timeNanos)}</div>
            <div class="${Be(h.status)}">${Ue(h.timeNanos)}</div>
            <div class="row-status">
              <span class="pill pill-${h.status}">${he(h.status)}</span>
              <span class="row-open">View</span>
            </div>
          </button>`).join(""):'<div class="empty-cell">No listener timing data yet. Attach the EventLens agent for per-listener metrics.</div>',r=t.recentTraces,a=Math.max(1,Math.ceil(r.length/Q));A=Math.min(A,a-1);const l=A*Q,u=r.slice(l,l+Q),p=u.length>0?u.map(h=>Ms(h)).join(""):'<div class="empty-cell">No dispatches captured yet.</div>',v=l+u.length,E=r.length>Q?`
          <div class="feed-pager">
            <button type="button" class="btn-ghost" id="feed-newer"${A<=0?" disabled":""}>← Newer</button>
            <span class="mono status-muted">${l+1}–${v} of ${r.length}</span>
            <button type="button" class="btn-ghost" id="feed-older"${A>=a-1?" disabled":""}>Older →</button>
          </div>`:"";e.innerHTML=`
    <section class="page">
      ${H(s.session.state)}
      <div class="stat-grid">
        <div class="stat-card">
          <div class="stat-label">Uptime</div>
          <div class="stat-value" id="stat-session-uptime">${ce(_e(s.session))}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">Events traced</div>
          <div class="stat-value">${t.eventsTraced.toLocaleString()}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">Listeners invoked</div>
          <div class="stat-value">${t.listenersInvoked.toLocaleString()}</div>
          <div class="stat-hint">avg ${t.avgListenersPerEvent.toFixed(1)}/event</div>
        </div>
        <div class="stat-card${i>0?" stat-card-alert":""}">
          <div class="stat-label">Flagged listeners</div>
          <div class="stat-value${i>0?" stat-value-danger":""}">${i}</div>
        </div>
      </div>
      <div class="stack-panels">
        <div class="panel">
          <div class="panel-head">Top offenders</div>
          <div class="offender-grid">
            <div>Name</div>
            <div>Event type</div>
            <div>Execution time</div>
            <div>%</div>
            <div>Status</div>
          </div>
          ${o}
        </div>
        <div class="panel">
          <div class="panel-head">Recent dispatches</div>
          <div class="feed-head">
            <div>Timestamp</div>
            <div>Event type</div>
            <div>Object</div>
            <div>Time</div>
            <div>Status</div>
          </div>
          ${p}
          ${E}
        </div>
      </div>
    </section>
  `,e.querySelectorAll("[data-sequence]").forEach(h=>{h.addEventListener("click",()=>{const X=Number(h.dataset.sequence);Number.isFinite(X)&&(n==null||n(X))})}),(k=e.querySelector("#feed-newer"))==null||k.addEventListener("click",()=>{A=Math.max(0,A-1),be(e,s,n)}),(f=e.querySelector("#feed-older"))==null||f.addEventListener("click",()=>{A=Math.min(a-1,A+1),be(e,s,n)})}function Ms(e){return`
          <button type="button" class="feed-row row-clickable" data-sequence="${e.sequence}">
            <div class="feed-time">${as(e.startedAtMillis)}</div>
            <div class="feed-event">${c(e.label)}</div>
            <div class="feed-detail">${e.detail?c(e.detail):"—"}</div>
            <div class="feed-ms ${Be(e.severity)}">${w(e.durationNanos)}</div>
            <div class="row-status">
              <span class="pill pill-${e.severity}">${he(e.severity)}</span>
              <span class="row-open">View</span>
            </div>
          </button>`}function Es(e,s){const n=e.querySelector("#stat-session-uptime");n&&(n.textContent=ce(s))}const Cs=["LOWEST","LOW","NORMAL","HIGH","HIGHEST","MONITOR"];let j=-1;function $e(){j=-1}function As(e,s){const n=e.dispatches.findIndex(t=>t.sequence===s);j=n>=0?n:-1}function ye(e,s){var p,v;const n=H(s.session.state);if(!s.dispatches.length){e.innerHTML=`<section class="page">${n}<p class="empty">No dispatches captured.</p></section>`;return}const i=j<0?s.dispatches.length-1:Math.min(j,s.dispatches.length-1),o=s.dispatches[i],r=z(o,s.dispatches),a=o.durationNanos/Z*100,l=ks(o),u=Os(o,r);e.innerHTML=`
    <section class="page">
      ${n}
      <div class="timeline-controls">
        <button type="button" class="btn-ghost" id="timeline-prev"${i<=0?" disabled":""}>← Previous</button>
        <span class="mono status-muted">dispatch ${i+1} / ${s.dispatches.length}</span>
        <button type="button" class="btn-ghost" id="timeline-next"${i>=s.dispatches.length-1?" disabled":""}>Next →</button>
      </div>
      <div class="dispatch-summary">
        <h1 class="dispatch-title">${c(b(o.eventClassName))}</h1>
        <div class="dispatch-facts">
          ${P("Sequence",`#${o.sequence}`)}
          ${P("Duration",`${w(o.durationNanos)} (${a.toFixed(1)}% of tick)`)}
          ${P("Location",l||"—")}
          ${P("Player",o.playerName||"—")}
          ${P("World",o.worldName||"—")}
          ${P("Server tick",o.serverTick!=null?String(o.serverTick):"—")}
          ${P("MSPT",o.msptMillis!=null?`${o.msptMillis.toFixed(1)} ms`:"—")}
        </div>
      </div>
      ${r?'<div class="page-notice page-notice-warmup" role="status"><span class="page-notice-label">WARMUP</span><span class="page-notice-text">First capture of this event type. Class-load and JIT time is excluded from flags and top offenders.</span></div>':""}
      <div class="handler-groups">${u}</div>
    </section>
  `,(p=e.querySelector("#timeline-prev"))==null||p.addEventListener("click",()=>{j=Math.max(0,i-1),ye(e,s)}),(v=e.querySelector("#timeline-next"))==null||v.addEventListener("click",()=>{j=Math.min(s.dispatches.length-1,i+1),ye(e,s)})}function P(e,s){return`<div><div class="dispatch-fact-label">${c(e)}</div><div class="dispatch-fact-value">${c(s)}</div></div>`}function ks(e){const s=[];return e.blockMaterial&&s.push(e.blockMaterial),e.blockX!=null&&e.blockY!=null&&e.blockZ!=null&&s.push(`(${e.blockX}, ${e.blockY}, ${e.blockZ})`),s.join(" ")}function Os(e,s){const n=B(e);if(!n.length)return`<p class="empty">${e.listenerTimings.length>0?"No listeners matched the timeline filters.":"No per-listener timings for this dispatch. Attach the EventLens agent for measured timings, or wait for the full trace sync."}</p>`;const t=new Map;for(const o of n){const r=xs(o.priority),a=t.get(r)??[];a.push(o),t.set(r,a)}const i=[];for(const o of Cs){const r=t.get(o);if(!(r!=null&&r.length))continue;r.sort((l,u)=>l.invocationOrder-u.invocationOrder);const a=r.map(l=>{const u=l.durationNanos/1e6,p=Ps(l,u,s),v=Hs(l);return`
          <div class="handler-row">
            <div class="handler-plugin">${c(l.pluginName)}</div>
            <div class="handler-method" title="${c(v)}">${c(v)}</div>
            <div class="handler-time">${w(l.durationNanos)}</div>
            <span class="pill pill-${p}">${he(p)}</span>
          </div>`}).join("");i.push(`<div class="handler-group"><div class="handler-priority">${qs(o)}</div>${a}</div>`)}return i.join("")}function Hs(e){const s=b(e.listenerClassName);return`${e.pluginName}.${s}#${e.methodName}`}function qs(e){return e==="HIGH"||e==="HIGHEST"?e==="HIGHEST"?"HIGHEST":"HIGH":e}function Ps(e,s,n){return n&&s<10?"warmup":e.exceedsSlowThreshold||s>=5?"critical":s>=2?"warn":"ok"}function xs(e){const s=e.toUpperCase();return s.includes("MONITOR")?"MONITOR":s.includes("LOWEST")?"LOWEST":s.includes("LOW")?"LOW":s.includes("HIGHEST")?"HIGHEST":s.includes("HIGH")?"HIGH":"NORMAL"}function Rs(e,s){const n=H(s.session.state),t=Vs(s.dispatches);if(!t){e.innerHTML=`<section class="page">${n}<p class="empty">No dispatches captured.</p></section>`;return}const i=B(t);if(!i.length){e.innerHTML=`<section class="page">${n}<p class="empty">No listener timing data. Attach the EventLens agent for flame view, or wait for the full trace sync.</p></section>`;return}const o=b(t.eventClassName),r=Math.max(t.durationNanos,1),a=[...i].sort((p,v)=>p.invocationOrder-v.invocationOrder),l=[...a].sort((p,v)=>v.durationNanos-p.durationNanos)[0],u=a.map(p=>{const v=p.durationNanos/r*100,E=p===l&&(p.exceedsSlowThreshold||v>=40);return`
        <div class="flame-item">
          <div class="flame-item-head">
            <span class="flame-item-name">${c(p.pluginName)}</span>
            <span class="flame-item-meta">${w(p.durationNanos)} — ${v.toFixed(0)}%</span>
          </div>
          <div class="flame-item-track">
            <div class="flame-item-bar${E?" dominant":""}" style="width:${Math.max(v,.6).toFixed(1)}%"></div>
            ${E?'<span class="flame-dominant">DOMINANT</span>':""}
          </div>
        </div>`}).join("");e.innerHTML=`
    <section class="page">
      ${n}
      <header class="page-header">
        <h1>${c(o)}</h1>
        <p class="page-subtitle">slowest dispatch — ${w(t.durationNanos)} total</p>
      </header>
      <div class="flame-list">${u}</div>
    </section>
  `}function Vs(e){return We(e,(s,n)=>n.durationNanos-s.durationNanos)}function Je(e,s,n){const t=n?js(n):Gs(s,null),i=n?b(n.session.eventClassName):t.eventLabel;if(!t.nodes.length){e.innerHTML='<section class="page"><p class="empty">No event graph data available.</p></section>';return}const o=t.nodes.map(a=>{const l=a.kind==="PLUGIN"?` x${a.weight}`:"";return`<div class="graph-pill${a.kind==="EVENT"?" event":""}">${c(a.label)}${l}<span class="graph-pill-kind">${a.kind}</span></div>`}).join(""),r=t.edges.map(a=>{const l=t.nodes.find(p=>p.id===a.sourceId),u=t.nodes.find(p=>p.id===a.targetId);return!l||!u?"":`<div class="graph-edge"><span>${c(l.label)}</span><span class="graph-edge-action">+ ${c(a.label)} →</span><span>${c(u.label)}</span><span class="graph-edge-weight">x${a.weight}</span></div>`}).join("");e.innerHTML=`
    <section class="page">
      ${n?H(n.session.state):""}
      <header class="page-header">
        <h1>${c(i||"Event graph")} — listener relationships</h1>
      </header>
      <div>
        <div class="graph-section-label">Nodes</div>
        <div class="graph-nodes">${o}</div>
        <div class="graph-section-label">Edges</div>
        <div class="graph-edges">${r||'<p class="empty">No listener edges for this event.</p>'}</div>
      </div>
    </section>
  `}function js(e){const s=b(e.session.eventClassName),n=new Map;for(const r of e.dispatches)for(const a of B(r))n.set(a.pluginName,(n.get(a.pluginName)??0)+1);const t=`event:${s}`,i=[{id:t,label:s,kind:"EVENT",weight:e.session.capturedEvents}],o=[];for(const[r,a]of[...n.entries()].sort((l,u)=>u[1]-l[1])){const l=`plugin:${r}`;i.push({id:l,label:r,kind:"PLUGIN",weight:a}),o.push({sourceId:l,targetId:t,weight:a,label:"listens"})}return{nodes:i,edges:o,eventLabel:s}}function Gs(e,s){const n=e.nodes.filter(a=>a.kind==="EVENT"),t=n.find(a=>s)??[...n].sort((a,l)=>l.weight-a.weight)[0]??null;if(!t)return{nodes:e.nodes.slice(0,8),edges:e.edges.slice(0,8),eventLabel:"Events"};const i=e.edges.filter(a=>a.sourceId===t.id||a.targetId===t.id),o=new Set([t.id,...i.flatMap(a=>[a.sourceId,a.targetId])]);return{nodes:e.nodes.filter(a=>o.has(a.id)).sort((a,l)=>a.kind==="EVENT"?-1:l.kind==="EVENT"?1:l.weight-a.weight),edges:i,eventLabel:t.label}}const Ne=["LOWEST","LOW","NORMAL","HIGH","MONITOR"];function Ds(e,s){if(!(s!=null&&s.dispatches.length)){const r=s?H(s.session.state):"";e.innerHTML=`<section class="page">${r}<p class="empty">No dispatch data for plugin graph.</p></section>`;return}const n=We(s.dispatches,(r,a)=>a.durationNanos-r.durationNanos);if(!n){e.innerHTML=`<section class="page">${H(s.session.state)}<p class="empty">No dispatch data for plugin graph.</p></section>`;return}const t=Ws(n),i=b(n.eventClassName),o=Ne.map(r=>{const a=t.filter(l=>l.priority===r||r==="HIGH"&&l.priority==="HIGHEST");return`
      <div class="priority-column">
        <div class="priority-label">${Fs(r)}</div>
        ${a.length?a.map(l=>`
              <div class="plugin-card card-${l.status}">
                <div class="plugin-name">${c(l.plugin)}</div>
                <div class="plugin-time">${w(l.durationNanos)}</div>
                <div class="plugin-tick">${Ue(l.durationNanos)}</div>
                ${l.readOnly?'<div class="plugin-note">read-only</div>':""}
              </div>`).join(""):""}
      </div>`}).join("");e.innerHTML=`
    <section class="page">
      ${H(s.session.state)}
      <header class="page-header">
        <h1>${c(i)}</h1>
        <p class="page-subtitle">slowest dispatch, by priority</p>
      </header>
      <div class="priority-grid">${o}</div>
    </section>
  `}function Fs(e){return e==="HIGH"?"HIGH / HIGHEST":e}function Ws(e){const s=[];for(const n of B(e)){const t=Us(n.priority),i=n.durationNanos/5e7*100,o=t==="MONITOR",r=o?"monitor":n.exceedsSlowThreshold||i>=10?"critical":i>=3?"warn":"ok",a=o||n.pluginName.toLowerCase().includes("eventlens");s.push({plugin:n.pluginName,priority:t,durationNanos:n.durationNanos,status:r,readOnly:a})}return s.sort((n,t)=>{const i=n.priority==="HIGHEST"?"HIGH":n.priority,o=t.priority==="HIGHEST"?"HIGH":t.priority,r=Ne.indexOf(i)-Ne.indexOf(o);return r!==0?r:t.durationNanos-n.durationNanos})}function Us(e){const s=e.toUpperCase();return s.includes("MONITOR")?"MONITOR":s.includes("LOWEST")?"LOWEST":s.includes("LOW")?"LOW":s.includes("HIGHEST")?"HIGHEST":s.includes("HIGH")?"HIGH":"NORMAL"}function _s(e,s){var a,l,u,p;const n=me(e),t=me(s),i=Xe(e),o=Xe(s),r=Bs(e,s);return[Ke("Event",b(e.session.eventClassName),b(s.session.eventClassName)),ee("Captured",e.session.capturedEvents,s.session.capturedEvents),ee("Dispatches",e.dispatches.length,s.dispatches.length),ee("Flagged listeners",n.flaggedListeners,t.flaggedListeners,!0),Ye("Avg dispatch",i,o),Ye("Slowest listener",((a=n.topOffenders[0])==null?void 0:a.timeNanos)??0,((l=t.topOffenders[0])==null?void 0:l.timeNanos)??0),Ke("Slowest plugin",((u=n.topOffenders[0])==null?void 0:u.plugin)??"—",((p=t.topOffenders[0])==null?void 0:p.plugin)??"—"),ee("Correlated pairs",r,r)]}function Ke(e,s,n){return{label:e,left:s,right:n,tone:s===n?"same":void 0}}function ee(e,s,n,t=!1){const i=n-s,o=i===0?"same":t?i<0?"down":"up":i>0?"up":"down";return{label:e,left:String(s),right:String(n),delta:i===0?"0":`${i>0?"+":""}${i}`,tone:o}}function Ye(e,s,n){const t=n-s,i=t===0?"same":t>0?"up":"down";return{label:e,left:w(s),right:w(n),delta:t===0?"0.00 ms":`${t>0?"+":"−"}${w(Math.abs(t))}`,tone:i}}function Xe(e){return e.dispatches.length?e.dispatches.reduce((n,t)=>n+t.durationNanos,0)/e.dispatches.length:0}function Bs(e,s){const n=new Set(s.dispatches.map(t=>t.correlationKey).filter(t=>!!t));return e.dispatches.filter(t=>t.correlationKey&&n.has(t.correlationKey)).length}function Js(e,s,n,t,i,o,r,a){var k;if(!s){e.innerHTML='<section class="page"><p class="empty">Select a live session or saved report first. That becomes the left side of the compare.</p></section>';return}const l=Se("Left",b(s.session.eventClassName),`${x(s.session.sessionId)} · ${s.session.capturedEvents} captured`,"Current view",!0),u=n?Se("Right",b(n.session.eventClassName),`${x(n.session.sessionId)} · ${n.session.capturedEvents} captured`,"Comparison",!0):Se("Right","Not selected","Choose a live session or a .json report","Waiting",!1),p=t.sessions.filter(f=>f.sessionId!==t.currentSessionId&&f.sessionId!==(n==null?void 0:n.session.sessionId)).map(f=>`<button type="button" class="compare-pick" data-session="${c(f.sessionId)}">
          <span class="compare-pick-title">${c(b(f.eventClassName))}</span>
          <span class="compare-pick-meta">${c(x(f.sessionId))} · ${f.capturedEvents} captured · live</span>
        </button>`).join(""),v=t.reports.filter(f=>Ks(f)&&f.fileName!==t.currentReportFile).map(f=>`<button type="button" class="compare-pick" data-report="${c(f.fileName)}">
          <span class="compare-pick-title">${c(f.fileName)}</span>
          <span class="compare-pick-meta">JSON report</span>
        </button>`).join(""),E=n?`<table class="data-table">
        <thead><tr><th>Metric</th><th>Left</th><th>Right</th><th>Delta</th></tr></thead>
        <tbody>${_s(s,n).map(f=>`<tr>
                <th>${c(f.label)}</th>
                <td>${c(f.left)}</td>
                <td>${c(f.right)}</td>
                <td class="compare-delta${f.tone?` delta-${f.tone}`:""}">${c(f.delta??"—")}</td>
              </tr>`).join("")}
        </tbody>
      </table>`:'<p class="compare-hint">Pick a counterpart on the right. Left stays as the session or report you already have open.</p>';e.innerHTML=`
    <section class="page">
      <header class="page-header">
        <h1>Compare</h1>
        <p class="page-subtitle">Left is the current view. The right side must be a live session or a <span class="mono">.json</span> report — HTML exports cannot be compared.</p>
      </header>
      <div class="compare-sides">
        ${l}
        <div class="compare-vs">vs</div>
        ${u}
      </div>
      ${n?'<button type="button" class="btn-ghost" id="compare-clear">Change right side</button>':`<div class="compare-picker">
        <div>
          <div class="sidebar-heading">Live sessions</div>
          <div class="compare-picks">${p||'<p class="sidebar-empty">No other live sessions</p>'}</div>
        </div>
        <div>
          <div class="sidebar-heading">JSON reports</div>
          <div class="compare-picks">${v||'<p class="sidebar-empty">No other JSON reports</p>'}</div>
        </div>
        <label class="file-upload compare-upload">Load JSON file<input type="file" accept="application/json,.json" /></label>
      </div>`}
      ${E}
    </section>`,Ys(e,i),e.querySelectorAll(".compare-pick[data-report]").forEach(f=>{f.addEventListener("click",()=>{const h=f.dataset.report;h&&(o==null||o(h))})}),e.querySelectorAll(".compare-pick[data-session]").forEach(f=>{f.addEventListener("click",()=>{const h=f.dataset.session;h&&(r==null||r(h))})}),(k=e.querySelector("#compare-clear"))==null||k.addEventListener("click",()=>{a==null||a()})}function Ks(e){return e.format?e.format.toLowerCase()==="json":e.fileName.toLowerCase().endsWith(".json")}function Se(e,s,n,t,i){return`
    <div class="compare-card${i?" filled":""}">
      <div class="compare-card-side">${c(e)}</div>
      <div class="compare-card-title">${c(s)}</div>
      <div class="compare-card-meta">${c(n)}</div>
      <span class="compare-card-tag">${c(t)}</span>
    </div>`}function Ys(e,s){const n=e.querySelector('input[type="file"]');!n||!s||n.addEventListener("change",()=>{var i;const t=(i=n.files)==null?void 0:i[0];t&&s(t)})}const m=document.querySelector("#app");let d=null,J=null,S="overview",g="",G="",y="offline",D=!1,se=!1,te=null,Le=!1,Te=0,ne="Paper —",we="1.3.2",K=I()?"live":"offline",Ze=[],$=null,ze=!1,F=null,ie=null,ae=null,Y=[];function L(){return g||(d==null?void 0:d.session.sessionId)||""}function O(e){const s=e.environment;if(s){const n=s.runtimeKind??"paper";if(n==="paper")ne=s.paperVersion??s.platformLabel??"Paper server";else{const t=s.loaderVersion??s.platformLabel??n;ne=`${n} client · ${t}`}}s!=null&&s.eventLensVersion&&(we=s.eventLensVersion),e.instrumentation&&(Le=e.instrumentation.agentPresent,Te=e.instrumentation.protocolVersion)}function Xs(e){$=e,Le=e.agentPresent,Te=e.protocolVersion,e.paperVersion&&(ne=e.paperVersion),e.eventLensVersion&&(we=e.eventLensVersion),!g&&!D&&e.activeTraceSessionId&&y!=="report"&&(g=e.activeTraceSessionId)}function Qe(){var s;const e=L();return e?((d==null?void 0:d.session.sessionId)===e?d.session.state:null)??((s=Y.find(n=>n.sessionId===e))==null?void 0:s.state)??null:(d==null?void 0:d.session.state)??null}function M(){return{activeView:S,liveAvailable:I(),sourceMode:K,streamConnected:se,agentPresent:Le,protocolVersion:Te,paperVersion:ne,eventLensVersion:we,serverStatus:$,report:d,sessionState:Qe(),selectedSessionId:g,selectedReportFile:G,dataSource:y,onNavigate:e=>{S=e,oe(e),N(!1)},onSourceModeChange:e=>{K=e,je(m,e),e==="live"&&I()&&!g&&le(Y),C(m,M())},onSessionChange:e=>{Me(e,!0)},onReportChange:e=>{es(e)},onFileLoad:e=>{tt(e)}}}function W(){ze||($s(m,M()),ze=!0)}function oe(e){m.querySelectorAll(".nav-item[data-view]").forEach(s=>{s.classList.toggle("active",s.dataset.view===e)})}function Ie(e){const s=cs(e);if(!s)return!1;const n=L();return n&&n!==s.sessionId?!1:(g||(g=s.sessionId,y="session",_(m,s.sessionId,"")),d=fs(d,s),C(m,M()),fe(m,s.sessionId,s.capturedEvents,s.eventClassName,s.state),N(!1),Zs(),!0)}function Zs(){F!==null&&window.clearTimeout(F),F=window.setTimeout(()=>{F=null,et()},400)}async function zs(){if(!(y!=="session"||!L()))try{const e=await R(L());if(L()!==e.session.sessionId)return;d=e,O(e),C(m,M()),fe(m,e.session.sessionId,e.session.capturedEvents,e.session.eventClassName,e.session.state)}catch{}}function Qs(){return d!=null&&d.dispatches.length?d.dispatches.some(e=>{var s;return e.listenerTimings.length===0&&(((s=e.listenerChain)==null?void 0:s.length)??0)===0}):!0}async function et(){if(!(y!=="session"||!L()))try{const e=await R(L());if(L()!==e.session.sessionId)return;d=e,O(e),C(m,M()),fe(m,e.session.sessionId,e.session.capturedEvents,e.session.eventClassName,e.session.state),N(!1)}catch{}}async function re(){const[e,s]=await Promise.all([Ae(),ke()]);return Y=e,Ze=s,ys(m,e,s,g,G),e}async function Me(e,s){g=e,G="",y="session",K="live",D=s,$e(),ge(),_(m,e,""),d=await R(e),O(d),C(m,M()),await N(!1)}async function es(e){G=e,g="",y="report",K="offline",D=!1,$e(),ge(),_(m,"",e),d=await Oe(e),O(d),C(m,M()),await N(!1)}async function st(){if(I())return;const e=window.__EVENTLENS_REPORT__;if(e&&typeof e=="object"){d=e,y="offline",O(d),W(),await N(!1);return}const s=new URLSearchParams(window.location.search).get("report"),n=s&&s.length>0?s:"./report.json";try{const t=await fetch(n);if(!t.ok)return;d=de(await t.text()),y="offline",O(d),W(),await N(!1)}catch{}}async function tt(e){y="offline",K="offline",g="",G="",D=!1,$e(),ge(),d=de(await e.text()),O(d),S="overview",W(),C(m,M()),oe(S),await N(!1)}async function le(e){const s=e.filter(t=>t.state==="ACTIVE");if(!s.length||g!==""&&s.some(t=>t.sessionId===g))return!1;if(!g||!D||y!=="session"){const t=s[0];return await Me(t.sessionId,!1),!0}return!1}async function ss(){y!=="session"||!L()||(d=await R(L()),O(d),C(m,M()),await N(!1))}async function Ee(){if(I())try{const e=await rs();Xs(e),C(m,M())}catch{}}function nt(){const e=Qe();if(d&&e&&He(e))return d.session.startedAtMillis;if($!=null&&$.activeTraceSessionId&&$.activeTraceStartedAtMillis>0){const n=$.activeTraceSessionId;if(!L()||L()===n)return $.activeTraceStartedAtMillis}const s=Y.find(n=>n.sessionId===L());return s&&He(s.state)?s.startedAtMillis:null}function it(){const e=nt();if(e==null)return;const s=Math.max(0,Date.now()-e),n=m.querySelector("#view-root");n&&S==="overview"&&Es(n,s)}function at(){ae===null&&(ae=window.setInterval(it,1e3))}function ot(){ie===null&&(ie=window.setInterval(()=>{Ee()},2e3))}async function ts(){if(!I())return;await Ee();const e=await re();await le(e)||y==="session"&&g&&await ss()}function rt(e,s){if(e==="session-started"){if(Ie(s))return;(async()=>{await re();const n=typeof s.sessionId=="string"?s.sessionId:"";n&&(!L()||!D)?await Me(n,!1):await le(await Ae())})();return}if(e==="dispatch"){if(Ie(s))return;ss();return}if(e==="poll"){ts();return}if(e==="session-stopped"){if(Ie(s)){re();return}ts()}}async function lt(){if(W(),!I())return;await Ee(),C(m,M()),ot(),at();const e=await re(),s=await ke();if(e.length){if(await le(e),!g&&($!=null&&$.activeTraceSessionId)&&(g=$.activeTraceSessionId,y="session"),g&&!d)try{d=await R(g),O(d),_(m,g,"")}catch{d=null}}else s.length&&s[0].format==="json"&&await es(s[0].fileName);const n=gs((i,o)=>{rt(i,o)});te=n.stop;const t=()=>{const i=n.connected();i!==se&&(se=i,xe(m,se,I()))};t(),window.setInterval(t,1e3)}function N(e=!0){W();const s=m.querySelector("#view-root");return s?(oe(S),e&&I()&&y==="session"&&!d?(s.innerHTML='<p class="empty">Refreshing live trace data…</p>',Promise.resolve()):(I()&&y==="session"&&Qs()?zs():Promise.resolve()).then(()=>{if(S==="events"){if(d){Je(s,{nodes:[],edges:[]},d);return}ls().then(t=>Je(s,t,d)).catch(()=>{s.innerHTML='<section class="page"><p class="empty">Failed to load event graph.</p></section>'});return}if(S==="plugins"){Ds(s,d);return}if(!d){if(!I()){s.innerHTML='<section class="page"><p class="empty">No report loaded. Open <span class="mono">report.json</span> with the file picker, or re-export the bundle.</p></section>';return}if($!=null&&$.activeTraceSessionId){const t=$.activeTraceStartedAtMillis>0?Math.max(0,Date.now()-$.activeTraceStartedAtMillis):0;s.innerHTML=`
          <section class="page">
            <div class="stat-grid">
              <div class="stat-card">
                <div class="stat-label">Uptime</div>
                <div class="stat-value" id="stat-session-uptime">${ce(t)}</div>
              </div>
            </div>
            <p class="empty">Interact in-game to capture events, or wait for the live feed to populate.</p>
          </section>`}else s.innerHTML='<section class="page"><p class="empty">No active trace session. Start one with <span class="mono">/eventlens trace start &lt;Event&gt;</span>.</p></section>';return}S==="overview"?be(s,d,t=>{As(d,t),S="timeline",oe(S),N(!1)}):S==="timeline"?ye(s,d):S==="flame"?Rs(s,d):S==="compare"&&Js(s,d,J,{reports:Ze,sessions:Y,currentReportFile:G,currentSessionId:g||d.session.sessionId},t=>{t.text().then(i=>{J=de(i),N(!1)})},t=>{Oe(t).then(i=>{J=i,N(!1)})},t=>{R(t).then(i=>{J=i,N(!1)})},()=>{J=null,N(!1)})})):Promise.resolve()}W(),st().then(()=>lt()).then(()=>N(!1)).catch(e=>{const s=m.querySelector("#view-root");s&&(s.innerHTML=`<p class="empty">Failed to load dashboard: ${e}</p>`)}),window.addEventListener("beforeunload",()=>{te==null||te(),F!==null&&window.clearTimeout(F),ie!==null&&window.clearInterval(ie),ae!==null&&window.clearInterval(ae)})})();
