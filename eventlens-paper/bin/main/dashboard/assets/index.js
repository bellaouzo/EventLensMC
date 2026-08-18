(function(){"use strict";function E(e){return`${(e/1e6).toFixed(2)} ms`}function z(e){return`${Math.max(0,Math.round(e))}ms`}function me(e){return!e||e==="—"?"—":e.length>10?e.slice(0,8):e}function N(e){const s=e.lastIndexOf(".");return s>=0?e.substring(s+1):e}function d(e){return e.replaceAll("&","&amp;").replaceAll("<","&lt;").replaceAll(">","&gt;").replaceAll('"',"&quot;")}function qe(e){return e.toLocaleTimeString([],{hour:"2-digit",minute:"2-digit",second:"2-digit",hour12:!1})}function je(e){const s=new Date(e),n=qe(s),t=String(s.getMilliseconds()).padStart(3,"0");return`${n}.${t}`}const Ve=()=>window.location.protocol.startsWith("http")&&window.location.port!=="";async function xe(){return(await fetch("/api/status")).json()}async function he(){return(await(await fetch("/api/sessions")).json()).sessions??[]}async function ge(){return(await(await fetch("/api/reports")).json()).reports??[]}async function R(e){const s=await fetch(`/api/sessions/${encodeURIComponent(e)}/report`);if(!s.ok)throw new Error("Session report unavailable");return s.json()}async function be(e){const s=await fetch(`/api/reports/${encodeURIComponent(e)}`);if(!s.ok)throw new Error("Report file unavailable");return s.json()}async function Ge(){return(await fetch("/api/graph/events")).json()}function $(){return Ve()}function Q(e){return JSON.parse(e)}function De(e){if(typeof e.sessionId!="string")return null;const s={sessionId:e.sessionId,eventClassName:typeof e.eventClassName=="string"?e.eventClassName:"",state:typeof e.state=="string"?e.state:"ACTIVE",capturedEvents:typeof e.capturedEvents=="number"?e.capturedEvents:0,durationMillis:typeof e.durationMillis=="number"?e.durationMillis:0,startedAtMillis:typeof e.startedAtMillis=="number"?e.startedAtMillis:Date.now()};return e.dispatch&&typeof e.dispatch=="object"&&(s.dispatch=Fe(e.dispatch)),s}function Fe(e){const s=Array.isArray(e.listenerTimings)?e.listenerTimings.filter(t=>typeof t=="object"&&t!==null).map(We):[],n=Array.isArray(e.listenerChain)?e.listenerChain.filter(t=>typeof t=="object"&&t!==null).map(Ue):void 0;return{sequence:typeof e.sequence=="number"?e.sequence:0,startedAtMillis:typeof e.startedAtMillis=="number"?e.startedAtMillis:0,durationNanos:typeof e.durationNanos=="number"?e.durationNanos:0,durationMillis:typeof e.durationMillis=="string"?e.durationMillis:"0ms",eventLensOverheadNanos:typeof e.eventLensOverheadNanos=="number"?e.eventLensOverheadNanos:0,eventClassName:typeof e.eventClassName=="string"?e.eventClassName:"",cancelledAtStart:e.cancelledAtStart===!0,cancelledAtEnd:e.cancelledAtEnd===!0,playerName:typeof e.playerName=="string"?e.playerName:null,worldName:typeof e.worldName=="string"?e.worldName:null,blockX:typeof e.blockX=="number"?e.blockX:null,blockY:typeof e.blockY=="number"?e.blockY:null,blockZ:typeof e.blockZ=="number"?e.blockZ:null,listenerChain:n,listenerTimings:s}}function Ue(e){return{registrationOrder:typeof e.registrationOrder=="number"?e.registrationOrder:void 0,pluginName:typeof e.pluginName=="string"?e.pluginName:"",listenerClassName:typeof e.listenerClassName=="string"?e.listenerClassName:"",methodName:typeof e.methodName=="string"?e.methodName:"",priority:typeof e.priority=="string"?e.priority:"NORMAL"}}function We(e){return{invocationOrder:typeof e.invocationOrder=="number"?e.invocationOrder:0,pluginName:typeof e.pluginName=="string"?e.pluginName:"",listenerClassName:typeof e.listenerClassName=="string"?e.listenerClassName:"",methodName:typeof e.methodName=="string"?e.methodName:"",priority:typeof e.priority=="string"?e.priority:"NORMAL",durationNanos:typeof e.durationNanos=="number"?e.durationNanos:0,durationMillis:typeof e.durationMillis=="string"?e.durationMillis:typeof e.durationMillis=="number"?`${e.durationMillis}ms`:"0ms",exceedsSlowThreshold:e.exceedsSlowThreshold===!0,threwException:e.threwException===!0,exceptionType:typeof e.exceptionType=="string"?e.exceptionType:null}}function Be(e,s){const n=s.dispatch,t=e&&e.session.sessionId===s.sessionId?e:Ke(s),a=[...t.dispatches];if(n){const i=a.findIndex(o=>o.sequence===n.sequence);i>=0?a[i]=_e(a[i],n):a.push(n)}const r=Math.max(s.capturedEvents,a.length);return{...t,session:{...t.session,sessionId:s.sessionId,eventClassName:s.eventClassName||t.session.eventClassName,state:s.state,capturedEvents:r,durationMillis:s.durationMillis,startedAtMillis:s.startedAtMillis},dispatches:a}}function _e(e,s){const n=s.listenerTimings.length>0?s.listenerTimings:e.listenerTimings,t=s.listenerChain&&s.listenerChain.length>0?s.listenerChain:e.listenerChain;return{...s,listenerTimings:n,listenerChain:t}}function Ke(e){return{reportVersion:"live",redactionMode:"live",session:{sessionId:e.sessionId,eventClassName:e.eventClassName,state:e.state,ownerName:"",startedAtMillis:e.startedAtMillis,durationMillis:e.durationMillis,capturedEvents:e.capturedEvents,droppedEvents:0,sampledOutEvents:0,filters:""},warnings:[],dispatches:e.dispatch?[e.dispatch]:[]}}const Ye=2e3;function Xe(e){let s=!0,n=!1,t=null,a=null;const r=()=>{n=!0,a!==null&&(window.clearInterval(a),a=null)},i=()=>{!s||a!==null||(a=window.setInterval(()=>{e("poll",{})},Ye))},o=()=>{!s||t||(t=new EventSource("/api/stream"),t.onopen=()=>{r()},t.addEventListener("connected",()=>{r()}),t.addEventListener("dispatch",l=>{ee(e,"dispatch",l.data)}),t.addEventListener("session-started",l=>{ee(e,"session-started",l.data)}),t.addEventListener("session-stopped",l=>{ee(e,"session-stopped",l.data)}),t.onerror=()=>{n=!1,t==null||t.close(),t=null,i(),s&&window.setTimeout(o,1e3)})};return o(),{stop:()=>{s=!1,n=!1,t==null||t.close(),t=null,a!==null&&window.clearInterval(a)},connected:()=>n}}function ee(e,s,n){try{e(s,JSON.parse(n))}catch{e(s,{})}}function Ne(e){return e==="ACTIVE"||e==="THROTTLED"}function D(e){switch(e){case"ACTIVE":return{label:"Active",shortLabel:"ACTIVE",tone:"active",isActive:!0};case"THROTTLED":return{label:"Throttled",shortLabel:"THROTTLED",tone:"warn",isActive:!0};case"STOPPED":return{label:"Stopped",shortLabel:"STOPPED",tone:"stopped",isActive:!1};case"FULL":return{label:"Full",shortLabel:"FULL",tone:"stopped",isActive:!1};case"EXPIRED":return{label:"Expired",shortLabel:"EXPIRED",tone:"stopped",isActive:!1};case"ABANDONED":return{label:"Abandoned",shortLabel:"ABANDONED",tone:"stopped",isActive:!1};default:return{label:e,shortLabel:e,tone:"stopped",isActive:!1}}}function C(e){const s=D(e);return s.tone!=="stopped"?"":`
    <div class="page-notice page-notice-stopped" role="status">
      <span class="page-notice-dot"></span>
      <span class="page-notice-label">${d(s.shortLabel)}</span>
      <span class="page-notice-text">${d("This trace has ended. You are viewing a frozen snapshot — data will not update.")}</span>
    </div>`}const Je=[{id:"overview",label:"Overview"},{id:"timeline",label:"Timeline"},{id:"flame",label:"Flame graph"},{id:"events",label:"Event graph"},{id:"plugins",label:"Plugin graph"},{id:"compare",label:"Compare"}];function Ze(e,s){var n,t,a;e.className="app-shell",e.innerHTML=`
    <header class="app-header">
      <div class="brand">
        <div class="brand-row">
          <span class="brand-name">EventLens</span>
          <span class="brand-version" id="brand-version">v${d(s.eventLensVersion)}</span>
        </div>
        <span class="brand-sub">diagnostics — observer only</span>
      </div>
      <nav class="top-nav">
        ${Je.map(r=>`
          <button type="button" class="nav-item${s.activeView===r.id?" active":""}" data-view="${r.id}">
            ${r.label}
          </button>`).join("")}
      </nav>
      <div class="header-session" id="header-session">${$e(s)}</div>
    </header>
    <div class="app-body">
      <aside class="sidebar">
        <div class="mode-toggle">
          <button type="button" class="mode-btn${s.sourceMode==="live"?" active":""}" data-mode="live"${s.liveAvailable?"":" disabled"}>Live</button>
          <button type="button" class="mode-btn${s.sourceMode==="offline"?" active":""}" data-mode="offline">Offline</button>
        </div>
        <section class="sidebar-section">
          <div class="sidebar-heading">Live sessions</div>
          <div id="stream-status">${Le(s.streamConnected,s.liveAvailable)}</div>
          <div id="session-list" class="session-list"></div>
        </section>
        <section class="sidebar-section">
          <div class="sidebar-heading">Saved reports</div>
          <div id="report-list" class="report-list"></div>
          <label class="file-upload">
            <input type="file" id="file-input" accept=".json,application/json" />
            Load JSON
          </label>
        </section>
        <section class="sidebar-section context-section">
          <div class="sidebar-heading">Context</div>
          <dl id="context-list" class="context-list">${Te(s)}</dl>
        </section>
      </aside>
      <main id="view-root" class="view-root"></main>
    </div>
  `,e.querySelectorAll(".nav-item[data-view]").forEach(r=>{r.addEventListener("click",()=>{s.onNavigate(r.dataset.view)})}),e.querySelectorAll(".mode-btn[data-mode]").forEach(r=>{r.addEventListener("click",()=>{r.disabled||s.onSourceModeChange(r.dataset.mode)})}),(n=e.querySelector("#session-list"))==null||n.addEventListener("click",r=>{const i=r.target.closest("[data-session-id]");i!=null&&i.dataset.sessionId&&s.onSessionChange(i.dataset.sessionId)}),(t=e.querySelector("#report-list"))==null||t.addEventListener("click",r=>{const i=r.target.closest("[data-report]");i!=null&&i.dataset.report&&s.onReportChange(i.dataset.report)}),(a=e.querySelector("#file-input"))==null||a.addEventListener("change",r=>{var l;const o=(l=r.target.files)==null?void 0:l[0];o&&s.onFileLoad(o)})}function se(e,s,n,t,a){const r=e.querySelector(`[data-session-id="${ts(s)}"]`);if(!r)return;if(a){r.dataset.state=a;const o=r.querySelector(".session-badge");if(o){const l=D(a);o.textContent=l.shortLabel,o.className=`session-badge badge-${l.tone}`}}const i=r.querySelector(".session-card-sub");i&&(i.textContent=`${N(t)} — ${n} captured`)}function P(e,s,n){e.querySelectorAll("[data-session-id]").forEach(t=>{t.classList.toggle("selected",t.dataset.sessionId===s)}),e.querySelectorAll("[data-report]").forEach(t=>{t.classList.toggle("selected",!!n&&t.dataset.report===n)})}function ye(e,s,n){const t=e.querySelector("#stream-status");t&&(t.innerHTML=Le(s,n))}function ze(e,s,n,t,a){const r=e.querySelector("#session-list"),i=e.querySelector("#report-list");r&&(r.innerHTML=s.length?s.map(o=>Qe(o,o.sessionId===t&&!a)).join(""):'<p class="sidebar-empty">No live sessions</p>'),i&&(i.innerHTML=n.length?n.map(o=>es(o.fileName,o.fileName===a)).join(""):'<p class="sidebar-empty">No saved reports</p>')}function Se(e,s){e.querySelectorAll(".mode-btn[data-mode]").forEach(n=>{n.classList.toggle("active",n.dataset.mode===s)})}function I(e,s){const n=e.querySelector("#header-session");n&&(n.innerHTML=$e(s));const t=e.querySelector("#brand-version");t&&(t.textContent=`v${s.eventLensVersion}`);const a=e.querySelector("#context-list");a&&(a.innerHTML=Te(s)),ye(e,s.streamConnected,s.liveAvailable),Se(e,s.sourceMode),P(e,s.selectedSessionId,s.selectedReportFile)}function $e(e){const s=ss(e),n=e.sessionState;if(!s||s==="—")return'<span class="header-session-id">—</span>';const t=n?D(n):null,a=t?`<span class="header-state-badge badge-${t.tone}">${d(t.shortLabel)}</span>`:"";return`<span class="header-session-id">${d(me(s))}</span>${a}`}function Le(e,s){return s?`<div class="stream-status${e?" connected":""}"><span class="stream-dot${e?"":" off"}"></span>${e?"stream connected":"stream disconnected"}</div>`:'<div class="stream-status"><span class="stream-dot off"></span>offline viewer</div>'}function Qe(e,s){const n=D(e.state);return`
    <button type="button" class="session-card${s?" selected":""}" data-session-id="${te(e.sessionId)}" data-state="${te(e.state)}">
      <div class="session-card-top">
        <span class="session-id">${d(me(e.sessionId))}</span>
        <span class="session-badge badge-${n.tone}">${d(n.shortLabel)}</span>
      </div>
      <div class="session-card-sub">${d(N(e.eventClassName))} — ${e.capturedEvents} captured</div>
    </button>`}function es(e,s){return`<button type="button" class="report-item${s?" selected":""}" data-report="${te(e)}">${d(e)}</button>`}function Te(e){var g,T,x,Z;const s=e.report,n=e.serverStatus,t=s==null?void 0:s.session,a=e.dataSource==="session"?"live session":e.dataSource==="report"?"saved report":"offline file",r=((g=s==null?void 0:s.dispatches.find(G=>G.worldName))==null?void 0:g.worldName)??(n==null?void 0:n.defaultWorldName)??"—",i=(n==null?void 0:n.tps)!=null?n.tps.toFixed(1):((x=(T=s==null?void 0:s.dispatches.find(G=>G.tps!=null))==null?void 0:T.tps)==null?void 0:x.toFixed(1))??"—",o=e.agentPresent?"present":"absent",l=e.protocolVersion>0?`v${e.protocolVersion}`:"—",p=((Z=s==null?void 0:s.instrumentation)==null?void 0:Z.mode)??(e.agentPresent?"precise":"dispatch");return[["Source",a],["Session ID",(t==null?void 0:t.sessionId)??(e.selectedSessionId||"—")],["State",(t==null?void 0:t.state)??e.sessionState??"—"],["Event",t?N(t.eventClassName):"—"],["Captured / dropped / sampled out",t?`${t.capturedEvents} / ${t.droppedEvents} / ${t.sampledOutEvents}`:"—"],["Owner",(t==null?void 0:t.ownerName)||"—"],["Filters",(t==null?void 0:t.filters)||"—"],["World",r],["Game mode",(n==null?void 0:n.defaultGameMode)||"—"],["Players online",n?String(n.onlinePlayers):"—"],["Server TPS",i],["Tick budget","50 ms"],["Runtime",e.paperVersion],["EventLens",`v${e.eventLensVersion}`],["Agent",o],["Protocol",l],["Mode",p],["Redaction",(s==null?void 0:s.redactionMode)||"—"]].map(([G,Gs])=>`
      <div class="context-row">
        <dt>${d(G)}</dt>
        <dd>${d(Gs)}</dd>
      </div>`).join("")}function ss(e){var s,n;return((s=e.report)==null?void 0:s.session.sessionId)??e.selectedSessionId??((n=e.serverStatus)==null?void 0:n.activeTraceSessionId)??"—"}function te(e){return d(e).replaceAll("'","&#39;")}function ts(e){return e.replaceAll("\\","\\\\").replaceAll('"','\\"')}function q(e){var a;if((((a=e.listenerTimings)==null?void 0:a.length)??0)>0)return e.listenerTimings;const s=e.listenerChain??[];if(s.length===0)return[];const n=Math.max(e.durationNanos,s.length),t=Math.floor(n/s.length);return s.map((r,i)=>{const o=i===s.length-1?n-t*(s.length-1):t;return{invocationOrder:r.registrationOrder??i+1,pluginName:r.pluginName,listenerClassName:r.listenerClassName,methodName:r.methodName,priority:r.priority,durationNanos:o,durationMillis:E(o),exceedsSlowThreshold:!1,threwException:!1,exceptionType:null}})}const F=5e7;function ns(e){var l;let s=0,n=0;const t=new Map;for(const p of e.dispatches)for(const u of q(p)){s+=1,u.exceedsSlowThreshold&&(n+=1);const g=`${u.pluginName}::${N(p.eventClassName)}`,T=t.get(g),x=u.durationNanos/F*100,Z=is(u,x);(!T||u.durationNanos>T.timeNanos)&&t.set(g,{plugin:u.pluginName,event:N(p.eventClassName),timeNanos:u.durationNanos,tickPercent:x,status:Z})}const a=[...t.values()].sort((p,u)=>u.timeNanos-p.timeNanos).slice(0,8),r=[...e.dispatches].sort((p,u)=>u.sequence-p.sequence).slice(0,8).map(p=>as(p)),i=e.session.capturedEvents,o=((l=e.dispatches.find(p=>p.worldName))==null?void 0:l.worldName)??null;return{uptimeMillis:Me(e.session),eventsTraced:i,listenersInvoked:s,avgListenersPerEvent:i>0?s/i:0,flaggedListeners:n,topOffenders:a,recentTraces:r,primaryWorld:o}}function is(e,s){return e.exceedsSlowThreshold||s>=10?"critical":s>=3?"warn":"ok"}function as(e){const s=[...e.listenerTimings??[]].sort((i,o)=>o.durationNanos-i.durationNanos)[0],n=e.durationNanos/F*100;let t="ok";n>=15||s!=null&&s.exceedsSlowThreshold?t="critical":n>=5&&(t="warn");const a=N(e.eventClassName),r=e.blockMaterial??e.playerName??null;return{sequence:e.sequence,label:a,detail:r,startedAtMillis:e.startedAtMillis,durationNanos:e.durationNanos,severity:t}}function ne(e){return e==="critical"?"CRITICAL":e==="warn"?"WARN":"OK"}function Ie(e){return`${(e/F*100).toFixed(1)}%`}function Me(e,s=Date.now()){return e.state==="ACTIVE"||e.state==="THROTTLED"?Math.max(0,s-e.startedAtMillis):e.durationMillis}function Ee(e){return e==="critical"?"offender-tick-crit":e==="warn"?"offender-tick-warn":"offender-tick-ok"}function os(e,s){const n=ns(s),t=n.flaggedListeners,a=n.topOffenders.length>0?n.topOffenders.map(i=>`
          <div class="offender-row">
            <div class="offender-plugin">${d(i.plugin)}</div>
            <div class="offender-event">${d(i.event)}</div>
            <div class="offender-time">${E(i.timeNanos)}</div>
            <div class="${Ee(i.status)}">${Ie(i.timeNanos)}</div>
            <div><span class="pill pill-${i.status}">${ne(i.status)}</span></div>
          </div>`).join(""):'<div class="empty-cell">No listener timing data yet. Attach the EventLens agent for per-listener metrics.</div>',r=n.recentTraces.length>0?n.recentTraces.map(i=>`
          <div class="feed-row">
            <div class="feed-time">${je(i.startedAtMillis)}</div>
            <div class="feed-event">${d(i.label)}</div>
            <div class="feed-detail">${i.detail?d(i.detail):"—"}</div>
            <div class="feed-ms ${Ee(i.severity)}">${E(i.durationNanos)}</div>
            <div><span class="pill pill-${i.severity}">${ne(i.severity)}</span></div>
          </div>`).join(""):'<div class="empty-cell">No dispatches captured yet.</div>';e.innerHTML=`
    <section class="page">
      ${C(s.session.state)}
      <div class="stat-grid">
        <div class="stat-card">
          <div class="stat-label">Uptime</div>
          <div class="stat-value" id="stat-session-uptime">${z(Me(s.session))}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">Events traced</div>
          <div class="stat-value">${n.eventsTraced.toLocaleString()}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">Listeners invoked</div>
          <div class="stat-value">${n.listenersInvoked.toLocaleString()}</div>
          <div class="stat-hint">avg ${n.avgListenersPerEvent.toFixed(1)}/event</div>
        </div>
        <div class="stat-card${t>0?" stat-card-alert":""}">
          <div class="stat-label">Flagged listeners</div>
          <div class="stat-value${t>0?" stat-value-danger":""}">${t}</div>
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
          ${a}
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
          ${r}
        </div>
      </div>
    </section>
  `}function rs(e,s){const n=e.querySelector("#stat-session-uptime");n&&(n.textContent=z(s))}const ls=["LOWEST","LOW","NORMAL","HIGH","HIGHEST","MONITOR"];let w=0;function ie(){w=0}function ae(e,s){var l,p;const n=C(s.session.state);if(!s.dispatches.length){e.innerHTML=`<section class="page">${n}<p class="empty">No dispatches captured.</p></section>`;return}w>=s.dispatches.length&&(w=0);const t=s.dispatches[w],a=w,r=t.durationNanos/F*100,i=cs(t),o=ds(t);e.innerHTML=`
    <section class="page">
      ${n}
      <div class="timeline-controls">
        <button type="button" class="btn-ghost" id="timeline-prev"${a<=0?" disabled":""}>← Previous</button>
        <span class="mono status-muted">dispatch ${a+1} / ${s.dispatches.length}</span>
        <button type="button" class="btn-ghost" id="timeline-next"${a>=s.dispatches.length-1?" disabled":""}>Next →</button>
      </div>
      <div class="dispatch-summary">
        <h1 class="dispatch-title">${d(N(t.eventClassName))}</h1>
        <div class="dispatch-facts">
          ${A("Sequence",`#${t.sequence}`)}
          ${A("Duration",`${E(t.durationNanos)} (${r.toFixed(1)}% of tick)`)}
          ${A("Location",i||"—")}
          ${A("Player",t.playerName||"—")}
          ${A("World",t.worldName||"—")}
          ${A("Server tick",t.serverTick!=null?String(t.serverTick):"—")}
          ${A("MSPT",t.msptMillis!=null?`${t.msptMillis.toFixed(1)} ms`:"—")}
        </div>
      </div>
      <div class="handler-groups">${o}</div>
    </section>
  `,(l=e.querySelector("#timeline-prev"))==null||l.addEventListener("click",()=>{w=Math.max(0,a-1),ae(e,s)}),(p=e.querySelector("#timeline-next"))==null||p.addEventListener("click",()=>{w=Math.min(s.dispatches.length-1,a+1),ae(e,s)})}function A(e,s){return`<div><div class="dispatch-fact-label">${d(e)}</div><div class="dispatch-fact-value">${d(s)}</div></div>`}function cs(e){const s=[];return e.blockMaterial&&s.push(e.blockMaterial),e.blockX!=null&&e.blockY!=null&&e.blockZ!=null&&s.push(`(${e.blockX}, ${e.blockY}, ${e.blockZ})`),s.join(" ")}function ds(e){const s=q(e);if(!s.length)return`<p class="empty">${e.listenerTimings.length>0?"No listeners matched the timeline filters.":"No per-listener timings for this dispatch. Attach the EventLens agent for measured timings, or wait for the full trace sync."}</p>`;const n=new Map;for(const r of s){const i=vs(r.priority),o=n.get(i)??[];o.push(r),n.set(i,o)}const t=Math.max(...s.map(r=>r.durationNanos),1),a=[];for(const r of ls){const i=n.get(r);if(!(i!=null&&i.length))continue;i.sort((l,p)=>l.invocationOrder-p.invocationOrder);const o=i.map(l=>{const p=l.durationNanos/1e6,u=fs(l,p),g=Math.max(18,l.durationNanos/t*100),T=us(l);return`
          <div class="handler-row">
            <div class="handler-plugin">${d(l.pluginName)}</div>
            <div class="handler-method" title="${d(T)}">${d(T)}</div>
            <div class="handler-time">${E(l.durationNanos)}</div>
            <div class="handler-bar ${u}" style="width:${g.toFixed(0)}%">${ne(u)}</div>
          </div>`}).join("");a.push(`<div><div class="handler-priority">${ps(r)}</div>${o}</div>`)}return a.join("")}function us(e){const s=N(e.listenerClassName);return`${e.pluginName}.${s}#${e.methodName}`}function ps(e){return e==="HIGH"||e==="HIGHEST"?e==="HIGHEST"?"HIGHEST":"HIGH":e}function fs(e,s){return e.exceedsSlowThreshold||s>=5?"critical":s>=2?"warn":"ok"}function vs(e){const s=e.toUpperCase();return s.includes("MONITOR")?"MONITOR":s.includes("LOWEST")?"LOWEST":s.includes("LOW")?"LOW":s.includes("HIGHEST")?"HIGHEST":s.includes("HIGH")?"HIGH":"NORMAL"}function ms(e,s){const n=C(s.session.state),t=hs(s.dispatches);if(!t){e.innerHTML=`<section class="page">${n}<p class="empty">No dispatches captured.</p></section>`;return}const a=q(t);if(!a.length){e.innerHTML=`<section class="page">${n}<p class="empty">No listener timing data. Attach the EventLens agent for flame view, or wait for the full trace sync.</p></section>`;return}const r=N(t.eventClassName),i=Math.max(t.durationNanos,1),o=[...a].sort((u,g)=>u.invocationOrder-g.invocationOrder),l=[...o].sort((u,g)=>g.durationNanos-u.durationNanos)[0],p=o.map(u=>{const g=u.durationNanos/i*100,T=u===l&&(u.exceedsSlowThreshold||g>=40);return`
        <div class="flame-item">
          <div class="flame-item-head">
            <span class="flame-item-name">${d(u.pluginName)}</span>
            <span class="flame-item-meta">${E(u.durationNanos)} — ${g.toFixed(0)}%</span>
          </div>
          <div class="flame-item-track">
            <div class="flame-item-bar${T?" dominant":""}" style="width:${Math.max(g,.6).toFixed(1)}%"></div>
            ${T?'<span class="flame-dominant">DOMINANT</span>':""}
          </div>
        </div>`}).join("");e.innerHTML=`
    <section class="page">
      ${n}
      <header class="page-header">
        <h1>${d(r)}</h1>
        <p class="page-subtitle">slowest dispatch — ${E(t.durationNanos)} total</p>
      </header>
      <div class="flame-list">${p}</div>
    </section>
  `}function hs(e){return e.length?[...e].sort((s,n)=>n.durationNanos-s.durationNanos)[0]:null}function we(e,s,n){const t=n?gs(n):bs(s,null),a=n?N(n.session.eventClassName):t.eventLabel;if(!t.nodes.length){e.innerHTML='<section class="page"><p class="empty">No event graph data available.</p></section>';return}const r=t.nodes.map(o=>{const l=o.kind==="PLUGIN"?` x${o.weight}`:"";return`<div class="graph-pill${o.kind==="EVENT"?" event":""}">${d(o.label)}${l}<span class="graph-pill-kind">${o.kind}</span></div>`}).join(""),i=t.edges.map(o=>{const l=t.nodes.find(u=>u.id===o.sourceId),p=t.nodes.find(u=>u.id===o.targetId);return!l||!p?"":`<div class="graph-edge"><span>${d(l.label)}</span><span class="graph-edge-action">+ ${d(o.label)} →</span><span>${d(p.label)}</span><span class="graph-edge-weight">x${o.weight}</span></div>`}).join("");e.innerHTML=`
    <section class="page">
      ${n?C(n.session.state):""}
      <header class="page-header">
        <h1>${d(a||"Event graph")} — listener relationships</h1>
      </header>
      <div>
        <div class="graph-section-label">Nodes</div>
        <div class="graph-nodes">${r}</div>
        <div class="graph-section-label">Edges</div>
        <div class="graph-edges">${i||'<p class="empty">No listener edges for this event.</p>'}</div>
      </div>
    </section>
  `}function gs(e){const s=N(e.session.eventClassName),n=new Map;for(const i of e.dispatches)for(const o of q(i))n.set(o.pluginName,(n.get(o.pluginName)??0)+1);const t=`event:${s}`,a=[{id:t,label:s,kind:"EVENT",weight:e.session.capturedEvents}],r=[];for(const[i,o]of[...n.entries()].sort((l,p)=>p[1]-l[1])){const l=`plugin:${i}`;a.push({id:l,label:i,kind:"PLUGIN",weight:o}),r.push({sourceId:l,targetId:t,weight:o,label:"listens"})}return{nodes:a,edges:r,eventLabel:s}}function bs(e,s){const n=e.nodes.filter(o=>o.kind==="EVENT"),t=n.find(o=>s)??[...n].sort((o,l)=>l.weight-o.weight)[0]??null;if(!t)return{nodes:e.nodes.slice(0,8),edges:e.edges.slice(0,8),eventLabel:"Events"};const a=e.edges.filter(o=>o.sourceId===t.id||o.targetId===t.id),r=new Set([t.id,...a.flatMap(o=>[o.sourceId,o.targetId])]);return{nodes:e.nodes.filter(o=>r.has(o.id)).sort((o,l)=>o.kind==="EVENT"?-1:l.kind==="EVENT"?1:l.weight-o.weight),edges:a,eventLabel:t.label}}const oe=["LOWEST","LOW","NORMAL","HIGH","MONITOR"];function Ns(e,s){if(!(s!=null&&s.dispatches.length)){const i=s?C(s.session.state):"";e.innerHTML=`<section class="page">${i}<p class="empty">No dispatch data for plugin graph.</p></section>`;return}const n=[...s.dispatches].sort((i,o)=>o.durationNanos-i.durationNanos)[0],t=Ss(n),a=N(n.eventClassName),r=oe.map(i=>{const o=t.filter(l=>l.priority===i||i==="HIGH"&&l.priority==="HIGHEST");return`
      <div class="priority-column">
        <div class="priority-label">${ys(i)}</div>
        ${o.length?o.map(l=>`
              <div class="plugin-card card-${l.status}">
                <div class="plugin-name">${d(l.plugin)}</div>
                <div class="plugin-time">${E(l.durationNanos)}</div>
                <div class="plugin-tick">${Ie(l.durationNanos)}</div>
                ${l.readOnly?'<div class="plugin-note">read-only</div>':""}
              </div>`).join(""):""}
      </div>`}).join("");e.innerHTML=`
    <section class="page">
      ${C(s.session.state)}
      <header class="page-header">
        <h1>${d(a)}</h1>
        <p class="page-subtitle">slowest dispatch, by priority</p>
      </header>
      <div class="priority-grid">${r}</div>
    </section>
  `}function ys(e){return e==="HIGH"?"HIGH / HIGHEST":e}function Ss(e){const s=[];for(const n of q(e)){const t=$s(n.priority),a=n.durationNanos/5e7*100,r=t==="MONITOR",i=r?"monitor":n.exceedsSlowThreshold||a>=10?"critical":a>=3?"warn":"ok",o=r||n.pluginName.toLowerCase().includes("eventlens");s.push({plugin:n.pluginName,priority:t,durationNanos:n.durationNanos,status:i,readOnly:o})}return s.sort((n,t)=>{const a=n.priority==="HIGHEST"?"HIGH":n.priority,r=t.priority==="HIGHEST"?"HIGH":t.priority,i=oe.indexOf(a)-oe.indexOf(r);return i!==0?i:t.durationNanos-n.durationNanos})}function $s(e){const s=e.toUpperCase();return s.includes("MONITOR")?"MONITOR":s.includes("LOWEST")?"LOWEST":s.includes("LOW")?"LOW":s.includes("HIGHEST")?"HIGHEST":s.includes("HIGH")?"HIGH":"NORMAL"}function Ls(e,s){const n=[{label:"Event",left:e.session.eventClassName,right:s.session.eventClassName},{label:"Captured",left:String(e.session.capturedEvents),right:String(s.session.capturedEvents)},{label:"Dispatches",left:String(e.dispatches.length),right:String(s.dispatches.length)}],t=Ts(e,s);return n.push({label:"Correlated pairs",left:String(t),right:String(t)}),n}function Ts(e,s){const n=new Set(s.dispatches.map(t=>t.correlationKey).filter(t=>!!t));return e.dispatches.filter(t=>t.correlationKey&&n.has(t.correlationKey)).length}function Is(e,s,n,t,a,r){if(!s){e.innerHTML='<section class="page"><p class="empty">Load a session or report first, then choose a second JSON to compare.</p></section>';return}if(!n){const o=t.filter(l=>l.fileName!==s.session.sessionId).map(l=>`<button type="button" class="compare-choice" data-report="${d(l.fileName)}">${d(l.fileName)}</button>`).join("");e.innerHTML=`
      <section class="page">
        <div class="compare-banner">Only one report is loaded. Choose a second report to compare.</div>
        <div class="compare-choices">
          <button type="button" class="compare-choice" disabled>${d(N(s.session.eventClassName))} — current ${s.session.sessionId?"session":"report"}</button>
          ${o}
        </div>
        <label class="file-upload">Load second JSON<input type="file" accept="application/json,.json" /></label>
      </section>`,Ae(e,a),Ms(e,r);return}const i=Ls(s,n).map(o=>`<tr><th>${d(o.label)}</th><td>${d(o.left)}</td><td>${d(o.right)}</td></tr>`).join("");e.innerHTML=`
    <section class="page">
      <label class="file-upload">Replace right report<input type="file" accept="application/json,.json" /></label>
      <table class="data-table">
        <thead><tr><th></th><th>Left</th><th>Right</th></tr></thead>
        <tbody>${i}</tbody>
      </table>
    </section>`,Ae(e,a)}function Ae(e,s){const n=e.querySelector('input[type="file"]');!n||!s||n.addEventListener("change",()=>{var a;const t=(a=n.files)==null?void 0:a[0];t&&s(t)})}function Ms(e,s){s&&e.querySelectorAll(".compare-choice[data-report]").forEach(n=>{n.addEventListener("click",()=>{const t=n.dataset.report;t&&s(t)})})}const f=document.querySelector("#app");let c=null,re=null,y="overview",v="",j="",h="offline",O=!1,U=!1,W=null,le=!1,ce=0,B="Paper —",de="1.3.0",V=$()?"live":"offline",Ce=[],m=null,Oe=!1,H=null,_=null,K=null,Y=[];function b(){return v||(c==null?void 0:c.session.sessionId)||""}function M(e){const s=e.environment;if(s){const n=s.runtimeKind??"paper";if(n==="paper")B=s.paperVersion??s.platformLabel??"Paper server";else{const t=s.loaderVersion??s.platformLabel??n;B=`${n} client · ${t}`}}s!=null&&s.eventLensVersion&&(de=s.eventLensVersion),e.instrumentation&&(le=e.instrumentation.agentPresent,ce=e.instrumentation.protocolVersion)}function Es(e){m=e,le=e.agentPresent,ce=e.protocolVersion,e.paperVersion&&(B=e.paperVersion),e.eventLensVersion&&(de=e.eventLensVersion),!v&&!O&&e.activeTraceSessionId&&h!=="report"&&(v=e.activeTraceSessionId)}function He(){var s;const e=b();return e?((c==null?void 0:c.session.sessionId)===e?c.session.state:null)??((s=Y.find(n=>n.sessionId===e))==null?void 0:s.state)??null:(c==null?void 0:c.session.state)??null}function L(){return{activeView:y,liveAvailable:$(),sourceMode:V,streamConnected:U,agentPresent:le,protocolVersion:ce,paperVersion:B,eventLensVersion:de,serverStatus:m,report:c,sessionState:He(),selectedSessionId:v,selectedReportFile:j,dataSource:h,onNavigate:e=>{y=e,ue(e),S()},onSourceModeChange:e=>{V=e,Se(f,e),e==="live"&&$()&&!v&&J(Y),I(f,L())},onSessionChange:e=>{fe(e,!0)},onReportChange:e=>{ke(e)},onFileLoad:e=>{ks(e)}}}function k(){Oe||(Ze(f,L()),Oe=!0)}function ue(e){f.querySelectorAll(".nav-item[data-view]").forEach(s=>{s.classList.toggle("active",s.dataset.view===e)})}function pe(e){const s=De(e);if(!s)return!1;const n=b();return n&&n!==s.sessionId?!1:(v||(v=s.sessionId,h="session",P(f,s.sessionId,"")),c=Be(c,s),I(f,L()),se(f,s.sessionId,s.capturedEvents,s.eventClassName,s.state),S(!1),ws(),!0)}function ws(){H!==null&&window.clearTimeout(H),H=window.setTimeout(()=>{H=null,Os()},400)}async function As(){if(!(h!=="session"||!b()))try{const e=await R(b());if(b()!==e.session.sessionId)return;c=e,M(e),I(f,L()),se(f,e.session.sessionId,e.session.capturedEvents,e.session.eventClassName,e.session.state)}catch{}}function Cs(){return c!=null&&c.dispatches.length?c.dispatches.some(e=>{var s;return e.listenerTimings.length===0&&(((s=e.listenerChain)==null?void 0:s.length)??0)===0}):!0}async function Os(){if(!(h!=="session"||!b()))try{const e=await R(b());if(b()!==e.session.sessionId)return;c=e,M(e),I(f,L()),se(f,e.session.sessionId,e.session.capturedEvents,e.session.eventClassName,e.session.state),S(!1)}catch{}}async function X(){const[e,s]=await Promise.all([he(),ge()]);return Y=e,Ce=s,ze(f,e,s,v,j),e}async function fe(e,s){v=e,j="",h="session",V="live",O=s,ie(),P(f,e,""),c=await R(e),M(c),I(f,L()),await S(!1)}async function ke(e){j=e,v="",h="report",V="offline",O=!1,ie(),P(f,"",e),c=await be(e),M(c),I(f,L()),await S(!1)}async function Hs(){if($())return;const e=window.__EVENTLENS_REPORT__;if(e&&typeof e=="object"){c=e,h="offline",M(c),k(),await S(!1);return}const s=new URLSearchParams(window.location.search).get("report"),n=s&&s.length>0?s:"./report.json";try{const t=await fetch(n);if(!t.ok)return;c=Q(await t.text()),h="offline",M(c),k(),await S(!1)}catch{}}async function ks(e){h="offline",V="offline",v="",j="",O=!1,ie(),c=Q(await e.text()),M(c),y="overview",k(),I(f,L()),ue(y),await S(!1)}async function J(e){const s=e.filter(t=>t.state==="ACTIVE");if(!s.length||v!==""&&s.some(t=>t.sessionId===v))return!1;if(!v||!O||h!=="session"){const t=s[0];return await fe(t.sessionId,!1),!0}return!1}async function Re(){h!=="session"||!b()||(c=await R(b()),M(c),I(f,L()),await S(!1))}async function ve(){if($())try{const e=await xe();Es(e),I(f,L())}catch{}}function Rs(){const e=He();if(c&&e&&Ne(e))return c.session.startedAtMillis;if(m!=null&&m.activeTraceSessionId&&m.activeTraceStartedAtMillis>0){const n=m.activeTraceSessionId;if(!b()||b()===n)return m.activeTraceStartedAtMillis}const s=Y.find(n=>n.sessionId===b());return s&&Ne(s.state)?s.startedAtMillis:null}function Ps(){const e=Rs();if(e==null)return;const s=Math.max(0,Date.now()-e),n=f.querySelector("#view-root");n&&y==="overview"&&rs(n,s)}function qs(){K===null&&(K=window.setInterval(Ps,1e3))}function js(){_===null&&(_=window.setInterval(()=>{ve()},2e3))}async function Pe(){if(!$())return;await ve();const e=await X();await J(e)||h==="session"&&v&&await Re()}function Vs(e,s){if(e==="session-started"){if(pe(s))return;(async()=>{await X();const n=typeof s.sessionId=="string"?s.sessionId:"";n&&(!b()||!O)?await fe(n,!1):await J(await he())})();return}if(e==="dispatch"){if(pe(s))return;Re();return}if(e==="poll"){Pe();return}if(e==="session-stopped"){if(pe(s)){X();return}Pe()}}async function xs(){if(k(),!$())return;await ve(),I(f,L()),js(),qs();const e=await X(),s=await ge();if(e.length){if(await J(e),!v&&(m!=null&&m.activeTraceSessionId)&&(v=m.activeTraceSessionId,h="session"),v&&!c)try{c=await R(v),M(c),P(f,v,"")}catch{c=null}}else s.length&&s[0].format==="json"&&await ke(s[0].fileName);const n=Xe((a,r)=>{Vs(a,r)});W=n.stop;const t=()=>{const a=n.connected();a!==U&&(U=a,ye(f,U,$()))};t(),window.setInterval(t,1e3)}function S(e=!0){k();const s=f.querySelector("#view-root");return s?(ue(y),e&&$()&&h==="session"&&!c?(s.innerHTML='<p class="empty">Refreshing live trace data…</p>',Promise.resolve()):($()&&h==="session"&&Cs()?As():Promise.resolve()).then(()=>{if(y==="events"){if(c){we(s,{nodes:[],edges:[]},c);return}Ge().then(t=>we(s,t,c)).catch(()=>{s.innerHTML='<section class="page"><p class="empty">Failed to load event graph.</p></section>'});return}if(y==="plugins"){Ns(s,c);return}if(!c){if(!$()){s.innerHTML='<section class="page"><p class="empty">No report loaded. Open <span class="mono">report.json</span> with the file picker, or re-export the bundle.</p></section>';return}if(m!=null&&m.activeTraceSessionId){const t=m.activeTraceStartedAtMillis>0?Math.max(0,Date.now()-m.activeTraceStartedAtMillis):0;s.innerHTML=`
          <section class="page">
            <div class="stat-grid">
              <div class="stat-card">
                <div class="stat-label">Uptime</div>
                <div class="stat-value" id="stat-session-uptime">${z(t)}</div>
              </div>
            </div>
            <p class="empty">Interact in-game to capture events, or wait for the live feed to populate.</p>
          </section>`}else s.innerHTML='<section class="page"><p class="empty">No active trace session. Start one with <span class="mono">/eventlens trace start &lt;Event&gt;</span>.</p></section>';return}y==="overview"?os(s,c):y==="timeline"?ae(s,c):y==="flame"?ms(s,c):y==="compare"&&Is(s,c,re,Ce,t=>{t.text().then(a=>{re=Q(a),S(!1)})},t=>{be(t).then(a=>{re=a,S(!1)})})})):Promise.resolve()}k(),Hs().then(()=>xs()).then(()=>S(!1)).catch(e=>{const s=f.querySelector("#view-root");s&&(s.innerHTML=`<p class="empty">Failed to load dashboard: ${e}</p>`)}),window.addEventListener("beforeunload",()=>{W==null||W(),H!==null&&window.clearTimeout(H),_!==null&&window.clearInterval(_),K!==null&&window.clearInterval(K)})})();
