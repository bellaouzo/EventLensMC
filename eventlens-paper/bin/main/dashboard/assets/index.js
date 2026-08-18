(function(){"use strict";function N(e){return`${(e/1e6).toFixed(2)}ms`}function O(e){const t=e.lastIndexOf(".");return t>=0?e.substring(t+1):e}function Q(e){const t=Math.floor(e/1e3),n=Math.floor(t/3600),s=Math.floor(t%3600/60),i=t%60;return n>0?`${n}h ${s}m ${i}s`:s>0?`${s}m ${i}s`:`${i}s`}function v(e){return e.replaceAll("&","&amp;").replaceAll("<","&lt;").replaceAll(">","&gt;").replaceAll('"',"&quot;")}function Oe(e){return e.toLocaleTimeString([],{hour:"2-digit",minute:"2-digit",second:"2-digit",hour12:!1})}function Ae(e){const t=new Date(e),n=Oe(t),s=String(t.getMilliseconds()).padStart(3,"0");return`${n}.${s}`}const He=()=>window.location.protocol.startsWith("http")&&window.location.port!=="";async function Pe(){return(await fetch("/api/status")).json()}async function ye(){return(await(await fetch("/api/sessions")).json()).sessions??[]}async function be(){return(await(await fetch("/api/reports")).json()).reports??[]}async function G(e){const t=await fetch(`/api/sessions/${encodeURIComponent(e)}/report`);if(!t.ok)throw new Error("Session report unavailable");return t.json()}async function Re(e){const t=await fetch(`/api/reports/${encodeURIComponent(e)}`);if(!t.ok)throw new Error("Report file unavailable");return t.json()}async function qe(){return(await fetch("/api/graph/events")).json()}function A(){return He()}function ee(e){return JSON.parse(e)}function Fe(e){if(typeof e.sessionId!="string")return null;const t={sessionId:e.sessionId,eventClassName:typeof e.eventClassName=="string"?e.eventClassName:"",state:typeof e.state=="string"?e.state:"ACTIVE",capturedEvents:typeof e.capturedEvents=="number"?e.capturedEvents:0,durationMillis:typeof e.durationMillis=="number"?e.durationMillis:0,startedAtMillis:typeof e.startedAtMillis=="number"?e.startedAtMillis:Date.now()};return e.dispatch&&typeof e.dispatch=="object"&&(t.dispatch=je(e.dispatch)),t}function je(e){const t=Array.isArray(e.listenerTimings)?e.listenerTimings.filter(s=>typeof s=="object"&&s!==null).map(Ge):[],n=Array.isArray(e.listenerChain)?e.listenerChain.filter(s=>typeof s=="object"&&s!==null).map(De):void 0;return{sequence:typeof e.sequence=="number"?e.sequence:0,startedAtMillis:typeof e.startedAtMillis=="number"?e.startedAtMillis:0,durationNanos:typeof e.durationNanos=="number"?e.durationNanos:0,durationMillis:typeof e.durationMillis=="string"?e.durationMillis:"0ms",eventLensOverheadNanos:typeof e.eventLensOverheadNanos=="number"?e.eventLensOverheadNanos:0,eventClassName:typeof e.eventClassName=="string"?e.eventClassName:"",cancelledAtStart:e.cancelledAtStart===!0,cancelledAtEnd:e.cancelledAtEnd===!0,playerName:typeof e.playerName=="string"?e.playerName:null,worldName:typeof e.worldName=="string"?e.worldName:null,blockX:typeof e.blockX=="number"?e.blockX:null,blockY:typeof e.blockY=="number"?e.blockY:null,blockZ:typeof e.blockZ=="number"?e.blockZ:null,listenerChain:n,listenerTimings:t}}function De(e){return{registrationOrder:typeof e.registrationOrder=="number"?e.registrationOrder:void 0,pluginName:typeof e.pluginName=="string"?e.pluginName:"",listenerClassName:typeof e.listenerClassName=="string"?e.listenerClassName:"",methodName:typeof e.methodName=="string"?e.methodName:"",priority:typeof e.priority=="string"?e.priority:"NORMAL"}}function Ge(e){return{invocationOrder:typeof e.invocationOrder=="number"?e.invocationOrder:0,pluginName:typeof e.pluginName=="string"?e.pluginName:"",listenerClassName:typeof e.listenerClassName=="string"?e.listenerClassName:"",methodName:typeof e.methodName=="string"?e.methodName:"",priority:typeof e.priority=="string"?e.priority:"NORMAL",durationNanos:typeof e.durationNanos=="number"?e.durationNanos:0,durationMillis:typeof e.durationMillis=="string"?e.durationMillis:typeof e.durationMillis=="number"?`${e.durationMillis}ms`:"0ms",exceedsSlowThreshold:e.exceedsSlowThreshold===!0,threwException:e.threwException===!0,exceptionType:typeof e.exceptionType=="string"?e.exceptionType:null}}function Ve(e,t){const n=t.dispatch,s=e&&e.session.sessionId===t.sessionId?e:Be(t),i=[...s.dispatches];if(n){const o=i.findIndex(c=>c.sequence===n.sequence);o>=0?i[o]=We(i[o],n):i.push(n)}const r=Math.max(t.capturedEvents,i.length);return{...s,session:{...s.session,sessionId:t.sessionId,eventClassName:t.eventClassName||s.session.eventClassName,state:t.state,capturedEvents:r,durationMillis:t.durationMillis,startedAtMillis:t.startedAtMillis},dispatches:i}}function We(e,t){const n=t.listenerTimings.length>0?t.listenerTimings:e.listenerTimings,s=t.listenerChain&&t.listenerChain.length>0?t.listenerChain:e.listenerChain;return{...t,listenerTimings:n,listenerChain:s}}function Be(e){return{reportVersion:"live",redactionMode:"live",session:{sessionId:e.sessionId,eventClassName:e.eventClassName,state:e.state,ownerName:"",startedAtMillis:e.startedAtMillis,durationMillis:e.durationMillis,capturedEvents:e.capturedEvents,droppedEvents:0,sampledOutEvents:0,filters:""},warnings:[],dispatches:e.dispatch?[e.dispatch]:[]}}const Ue=2e3;function _e(e){let t=!0,n=!1,s=null,i=null;const r=()=>{n=!0,i!==null&&(window.clearInterval(i),i=null)},o=()=>{!t||i!==null||(i=window.setInterval(()=>{e("poll",{})},Ue))},c=()=>{!t||s||(s=new EventSource("/api/stream"),s.onopen=()=>{r()},s.addEventListener("connected",()=>{r()}),s.addEventListener("dispatch",d=>{te(e,"dispatch",d.data)}),s.addEventListener("session-started",d=>{te(e,"session-started",d.data)}),s.addEventListener("session-stopped",d=>{te(e,"session-stopped",d.data)}),s.onerror=()=>{n=!1,s==null||s.close(),s=null,o(),t&&window.setTimeout(c,1e3)})};return c(),{stop:()=>{t=!1,n=!1,s==null||s.close(),s=null,i!==null&&window.clearInterval(i)},connected:()=>n}}function te(e,t,n){try{e(t,JSON.parse(n))}catch{e(t,{})}}const H={overview:'<svg width="16" height="16" viewBox="0 0 16 16"><rect x="1" y="1" width="6" height="6" rx="1.2" fill="currentColor"/><rect x="9" y="1" width="6" height="6" rx="1.2" fill="currentColor"/><rect x="1" y="9" width="6" height="6" rx="1.2" fill="currentColor"/><rect x="9" y="9" width="6" height="6" rx="1.2" fill="currentColor"/></svg>',timeline:'<svg width="16" height="16" viewBox="0 0 16 16"><line x1="1" y1="4" x2="15" y2="4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/><line x1="1" y1="8" x2="10" y2="8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/><line x1="1" y1="12" x2="13" y2="12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>',flame:'<svg width="16" height="16" viewBox="0 0 16 16"><rect x="1" y="10" width="3" height="5" fill="currentColor"/><rect x="6.5" y="6" width="3" height="9" fill="currentColor"/><rect x="12" y="2" width="3" height="13" fill="currentColor"/></svg>',events:'<svg width="16" height="16" viewBox="0 0 16 16"><line x1="3" y1="13" x2="8" y2="3" stroke="currentColor" stroke-width="1.3"/><line x1="13" y1="13" x2="8" y2="3" stroke="currentColor" stroke-width="1.3"/><line x1="3" y1="13" x2="13" y2="13" stroke="currentColor" stroke-width="1.3"/><circle cx="3" cy="13" r="1.8" fill="currentColor"/><circle cx="13" cy="13" r="1.8" fill="currentColor"/><circle cx="8" cy="3" r="1.8" fill="currentColor"/></svg>',plugins:'<svg width="16" height="16" viewBox="0 0 16 16"><line x1="2" y1="8" x2="8" y2="3" stroke="currentColor" stroke-width="1.3"/><line x1="2" y1="8" x2="8" y2="13" stroke="currentColor" stroke-width="1.3"/><line x1="8" y1="3" x2="14" y2="8" stroke="currentColor" stroke-width="1.3"/><line x1="8" y1="13" x2="14" y2="8" stroke="currentColor" stroke-width="1.3"/><circle cx="2" cy="8" r="1.6" fill="currentColor"/><circle cx="8" cy="3" r="1.6" fill="currentColor"/><circle cx="8" cy="13" r="1.6" fill="currentColor"/><circle cx="14" cy="8" r="1.6" fill="currentColor"/></svg>'};function $e(e){return e==="ACTIVE"||e==="THROTTLED"}function P(e){switch(e){case"ACTIVE":return{label:"Active",shortLabel:"ACTIVE",tone:"active",isActive:!0};case"THROTTLED":return{label:"Throttled",shortLabel:"THROTTLED",tone:"warn",isActive:!0};case"STOPPED":return{label:"Stopped",shortLabel:"STOPPED",tone:"stopped",isActive:!1};case"FULL":return{label:"Full",shortLabel:"FULL",tone:"stopped",isActive:!1};case"EXPIRED":return{label:"Expired",shortLabel:"EXPIRED",tone:"stopped",isActive:!1};case"ABANDONED":return{label:"Abandoned",shortLabel:"ABANDONED",tone:"stopped",isActive:!1};default:return{label:e,shortLabel:e,tone:"stopped",isActive:!1}}}function Ne(e){const t=P(e).tone;return t==="active"?"session-option-active":t==="warn"?"session-option-warn":"session-option-stopped"}function se(e){if(!e)return"";const t=P(e).tone;return t==="active"?"session-select-active":t==="warn"?"session-select-warn":"session-select-stopped"}function Se(e,t,n,s){const i=P(s),r=O(t);return`${e} · ${r} (${n}) · ${i.shortLabel}`}function R(e){const t=P(e);return t.tone!=="stopped"?"":`
    <div class="page-notice page-notice-stopped" role="status">
      <span class="page-notice-dot"></span>
      <span class="page-notice-label">${v(t.shortLabel)}</span>
      <span class="page-notice-text">${v("This trace has ended. You are viewing a frozen snapshot — data will not update.")}</span>
    </div>`}function ne(e,t){const n=t?"live stream":"refreshes every 2s",s=e?P(e):null;return!s||s.isActive?`
      <div id="trace-status-indicator" class="trace-status-group trace-status-live">
        <span class="trace-status-dot"></span>
        <span class="trace-status-label">LIVE</span>
        ${e==="THROTTLED"?'<span class="trace-throttle-pill">throttled</span>':""}
        <span class="status-muted">· ${v(n)}</span>
      </div>`:`
    <div id="trace-status-indicator" class="trace-status-group trace-status-stopped">
      <span class="trace-status-dot"></span>
      <span class="trace-status-label">${v(s.shortLabel)}</span>
      <span class="status-muted">· trace ended</span>
    </div>`}const Ye=[{id:"overview",label:"Overview",icon:H.overview},{id:"timeline",label:"Timeline",icon:H.timeline},{id:"flame",label:"Flame graph",icon:H.flame},{id:"events",label:"Event graph",icon:H.events},{id:"plugins",label:"Plugin graph",icon:H.plugins},{id:"compare",label:"Compare",icon:H.overview}];function Ke(e,t){var a,l,u,m;const n=we(t),s=t.sessionState,i=Le(t),r=t.agentPresent?`agent v${t.protocolVersion}`:"agent absent",o=(a=t.serverStatus)==null?void 0:a.tps,c=o!=null?o.toFixed(1):"—",d=o!=null&&o>=18?"tps-ok":"status-highlight";e.className="app-shell",e.innerHTML=`
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-mark">EL</div>
        <div class="brand-text">
          <span class="brand-name">EventLens</span>
          <span class="brand-sub">DIAGNOSTICS</span>
        </div>
      </div>
      <nav class="sidebar-nav">
        ${Ye.map(f=>`
          <button type="button" class="nav-item${t.activeView===f.id?" active":""}" data-view="${f.id}">
            <span class="nav-icon">${f.icon}</span>
            <span>${f.label}</span>
          </button>`).join("")}
      </nav>
      <div class="sidebar-footer">
        <span>${V(t.paperVersion)}</span><br>
        <span>${V(r)}</span>
      </div>
    </aside>
    <div class="main-column">
      <header class="status-bar">
        <div class="status-left">
          ${t.liveMode?`${ne(s,t.streamConnected)}<div class="status-divider"></div>`:'<span class="status-muted">Offline report</span><div class="status-divider"></div>'}
          <div class="status-mono">session <span class="status-highlight">${C(n)}</span></div>
          <div class="status-mono">world <span class="status-highlight">${C(i)}</span></div>
        </div>
        <div class="status-right">
          <div class="status-mono">TPS <span id="status-tps" class="${d}">${C(c)}</span></div>
          <div class="status-mono">tick budget <span class="status-highlight">50ms</span></div>
          <span class="agent-pill${t.agentPresent?"":" absent"}">${C(r)}</span>
        </div>
      </header>
      ${t.liveMode?`<div class="source-bar">
              <select id="session-select" class="source-select ${se(s)}">
                <option value="">Live session…</option>
              </select>
              <select id="report-select" class="source-select">
                <option value="">Saved report…</option>
              </select>
            </div>`:`<div class="source-bar">
              <label class="file-upload">
                <input type="file" id="file-input" accept=".json,application/json" />
                Load trace report JSON
              </label>
            </div>`}
      <main id="view-root" class="view-root"></main>
    </div>
  `,e.querySelectorAll(".nav-item[data-view]").forEach(f=>{f.addEventListener("click",()=>{t.onNavigate(f.dataset.view)})}),t.liveMode?((l=e.querySelector("#session-select"))==null||l.addEventListener("change",f=>{const y=f.target.value;y&&t.onSessionChange(y)}),(u=e.querySelector("#report-select"))==null||u.addEventListener("change",f=>{const y=f.target.value;y&&t.onReportChange(y)})):(m=e.querySelector("#file-input"))==null||m.addEventListener("change",async f=>{var M;const h=(M=f.target.files)==null?void 0:M[0];h&&t.onFileLoad(h)})}function ie(e,t,n,s,i){const r=e.querySelector("#session-select");if(r)for(const o of r.options){if(o.value!==t)continue;const c=i??o.dataset.state??"ACTIVE";o.className=Ne(c),o.dataset.state=c,o.textContent=Se(t,s,n,c);break}}function B(e,t,n){const s=e.querySelector("#session-select"),i=e.querySelector("#report-select");s&&t&&(s.value=t),i&&n?i.value=n:i&&t&&(i.value="")}function Xe(e,t,n){const s=e.querySelector("#trace-status-indicator");if(s){s.outerHTML=ne(n,t);return}const i=e.querySelector(".trace-status-group .status-muted");i&&(i.textContent=n&&!P(n).isActive?"· trace ended":`· ${t?"live stream":"refreshes every 2s"}`)}function Ze(e,t,n,s,i,r){var y;const o=e.querySelector("#session-select"),c=e.querySelector("#report-select");if(!o||!c)return;const d=o.value,a=c.value;o.innerHTML='<option value="">Live session…</option>'+t.map(h=>{const M=Ne(h.state),T=Se(h.sessionId,h.eventClassName,h.capturedEvents,h.state);return`<option value="${V(h.sessionId)}" class="${M}" data-state="${V(h.state)}">${C(T)}</option>`}).join(""),c.innerHTML='<option value="">Saved report…</option>'+n.map(h=>`<option value="${V(h.fileName)}">${C(h.fileName)}</option>`).join("");const l=s||d,u=i||a;l&&t.some(h=>h.sessionId===l)&&(o.value=l);const m=r??((y=t.find(h=>h.sessionId===o.value))==null?void 0:y.state)??null;o.classList.remove("session-select-active","session-select-warn","session-select-stopped");const f=se(m);f&&o.classList.add(f),u&&n.some(h=>h.fileName===u)?c.value=u:l&&(c.value="")}function x(e,t){var M;const n=we(t),s=t.sessionState,i=Le(t),r=t.agentPresent?`agent v${t.protocolVersion}`:"agent absent",o=(M=t.serverStatus)==null?void 0:M.tps,c=o!=null?o.toFixed(1):"—",d=e.querySelector(".status-left"),a=d==null?void 0:d.querySelector(".trace-status-group");if(d&&t.liveMode){const T=ne(s,t.streamConnected);a&&(a.outerHTML=T)}const l=e.querySelector("#session-select");if(l){l.classList.remove("session-select-active","session-select-warn","session-select-stopped");const T=se(s);T&&l.classList.add(T)}const u=e.querySelector(".status-left .status-highlight");u&&(u.textContent=n);const m=e.querySelectorAll(".status-left .status-highlight");m.length>=2&&(m[1].textContent=i);const f=e.querySelector("#status-tps");f&&(f.textContent=c,f.classList.toggle("tps-ok",o!=null&&o>=18),f.classList.toggle("status-highlight",o==null||o<18));const y=e.querySelector(".agent-pill");y&&(y.textContent=r,y.classList.toggle("absent",!t.agentPresent));const h=e.querySelector(".sidebar-footer");h&&(h.innerHTML=`<span>${C(t.paperVersion)}</span><br><span>${C(r)}</span>`)}function we(e){var t,n;return((t=e.report)==null?void 0:t.session.sessionId)??e.selectedSessionId??((n=e.serverStatus)==null?void 0:n.activeTraceSessionId)??"—"}function Le(e){var s,i;const t=(i=(s=e.report)==null?void 0:s.dispatches.find(r=>r.worldName))==null?void 0:i.worldName;if(t)return t;const n=e.serverStatus;return n!=null&&n.defaultWorldName?`${n.defaultWorldName} · ${n.defaultGameMode} · ${n.onlinePlayers} players`:"—"}function C(e){return e.replaceAll("&","&amp;").replaceAll("<","&lt;").replaceAll(">","&gt;").replaceAll('"',"&quot;")}function V(e){return C(e).replaceAll("'","&#39;")}function W(e){if(e.listenerTimings.length>0)return e.listenerTimings;const t=e.listenerChain??[];if(t.length===0)return[];const n=Math.max(e.durationNanos,t.length),s=Math.floor(n/t.length);return t.map((i,r)=>{const o=r===t.length-1?n-s*(t.length-1):s;return{invocationOrder:i.registrationOrder??r+1,pluginName:i.pluginName,listenerClassName:i.listenerClassName,methodName:i.methodName,priority:i.priority,durationNanos:o,durationMillis:N(o),exceedsSlowThreshold:!1,threwException:!1,exceptionType:null}})}const U=5e7;function ze(e){var d;let t=0,n=0;const s=new Map;for(const a of e.dispatches)for(const l of W(a)){t+=1,l.exceedsSlowThreshold&&(n+=1);const u=`${l.pluginName}::${O(a.eventClassName)}`,m=s.get(u),f=l.durationNanos/U*100,y=Je(l,f);(!m||l.durationNanos>m.timeNanos)&&s.set(u,{plugin:l.pluginName,event:O(a.eventClassName),timeNanos:l.durationNanos,tickPercent:f,status:y})}const i=[...s.values()].sort((a,l)=>l.timeNanos-a.timeNanos).slice(0,8),r=[...e.dispatches].sort((a,l)=>l.sequence-a.sequence).slice(0,8).map(a=>Qe(a)),o=e.session.capturedEvents,c=((d=e.dispatches.find(a=>a.worldName))==null?void 0:d.worldName)??null;return{uptimeMillis:Te(e.session),eventsTraced:o,listenersInvoked:t,avgListenersPerEvent:o>0?t/o:0,flaggedListeners:n,topOffenders:i,recentTraces:r,primaryWorld:c}}function Je(e,t){return e.exceedsSlowThreshold||t>=10?"critical":t>=3?"warn":"ok"}function Qe(e){const t=[...e.listenerTimings].sort((o,c)=>c.durationNanos-o.durationNanos)[0],n=e.durationNanos/U*100;let s="ok";n>=15||t!=null&&t.exceedsSlowThreshold?s="critical":n>=5&&(s="warn");const i=O(e.eventClassName),r=e.blockMaterial??e.playerName??null;return{sequence:e.sequence,label:i,detail:r,startedAtMillis:e.startedAtMillis,durationNanos:e.durationNanos,severity:s}}function et(e){return e==="critical"?"critical":e==="warn"?"warn":"ok"}function oe(e){return`${(e/U*100).toFixed(1)}%`}function Te(e,t=Date.now()){return e.state==="ACTIVE"||e.state==="THROTTLED"?Math.max(0,t-e.startedAtMillis):e.durationMillis}function tt(e){return e==="critical"?"offender-tick-crit":e==="warn"?"offender-tick-warn":"offender-tick-ok"}function st(e){return e==="critical"?"severity-crit":e==="warn"?"severity-warn":"severity-ok"}function nt(e,t){const n=ze(t),s=n.topOffenders.length>0?n.topOffenders.map(r=>`
          <div class="offender-row">
            <div class="offender-plugin">${v(r.plugin)}</div>
            <div class="offender-event">${v(r.event)}</div>
            <div class="offender-time">${N(r.timeNanos)}</div>
            <div class="${tt(r.status)}">${oe(r.timeNanos)}</div>
            <div><span class="pill pill-${r.status}">${et(r.status)}</span></div>
          </div>`).join(""):'<div class="empty-cell">No listener timing data yet. Attach the EventLens agent for per-listener metrics.</div>',i=n.recentTraces.length>0?n.recentTraces.map(r=>`
          <div class="feed-row">
            <div class="feed-time">${Ae(r.startedAtMillis)}</div>
            <div class="feed-body">
              ${v(r.label)}${r.detail?` <span class="feed-detail">${v(r.detail)}</span>`:""}
            </div>
            <div class="feed-ms ${st(r.severity)}">${N(r.durationNanos)}</div>
          </div>`).join(""):'<div class="empty-cell">No dispatches captured yet.</div>';e.innerHTML=`
    <section class="page">
      <header class="page-header">
        <h1>Overview</h1>
        <p class="page-subtitle">Session-wide listener activity across all traced event types.</p>
      </header>
      ${R(t.session.state)}
      <div class="stat-grid">
        <div class="stat-card">
          <div class="stat-label">Session uptime</div>
          <div class="stat-value" id="stat-session-uptime">${Q(Te(t.session))}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">Events traced</div>
          <div class="stat-value">${n.eventsTraced.toLocaleString()}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">Listeners invoked</div>
          <div class="stat-value">${n.listenersInvoked.toLocaleString()}</div>
          <div class="stat-hint">avg ${n.avgListenersPerEvent.toFixed(1)} / event</div>
        </div>
        <div class="stat-card stat-card-alert">
          <div class="stat-label">Flagged listeners</div>
          <div class="stat-value stat-value-danger">${n.flaggedListeners}</div>
          <div class="stat-hint">warn or critical this session</div>
        </div>
      </div>
      <div class="stack-panels">
        <div class="panel">
          <div class="panel-head">Top offenders</div>
          <div class="offender-grid">
            <div>Plugin</div>
            <div>Event</div>
            <div>Time</div>
            <div>% tick</div>
            <div>Status</div>
          </div>
          ${s}
        </div>
        <div class="panel">
          <div class="panel-head">Recent trace feed</div>
          <div class="feed-list">${i}</div>
        </div>
      </div>
    </section>
  `}function it(e,t){const n=e.querySelector("#stat-session-uptime");n&&(n.textContent=Q(t))}const ot=["LOWEST","LOW","NORMAL","HIGH","HIGHEST","MONITOR"];let q=0;function ae(){q=0}function re(e,t){var l,u;const n=R(t.session.state);if(!t.dispatches.length){e.innerHTML=`<section class="page"><header class="page-header"><h1>Timeline</h1></header>${n}<p class="empty">No dispatches captured.</p></section>`;return}q>=t.dispatches.length&&(q=0);const s=at(t.dispatches,q),i=t.dispatches.indexOf(s),r=Math.max(12,Math.ceil(s.durationNanos/1e6)+1),o=s.durationNanos/U*100,c=o>=15?"crit":o>=5?"warn":"ok",d=rt(s),a=ct(s,r);e.innerHTML=`
    <section class="page">
      <header class="page-header">
        <h1>Timeline</h1>
        <p class="page-subtitle mono">
          ${v(O(s.eventClassName))}${d?` · ${v(d)}`:""}${s.peerSessionId?` · linked ${v(s.peerSessionId)}`:""}${s.serverTick!=null?` · server tick ${s.serverTick}`:""}${s.msptMillis!=null?` · ${s.msptMillis.toFixed(1)} mspt`:""} · #${s.sequence} · total <span class="severity-${c}">${N(s.durationNanos)} (${o.toFixed(1)}% of tick budget)</span>
        </p>
      </header>
      ${n}
      <div class="timeline-panel">
        <div class="timeline-controls">
          <button type="button" class="btn-ghost" id="timeline-prev"${i<=0?" disabled":""}>← Previous</button>
          <span class="mono status-muted">Dispatch ${i+1} of ${t.dispatches.length}</span>
          <button type="button" class="btn-ghost" id="timeline-next"${i>=t.dispatches.length-1?" disabled":""}>Next →</button>
        </div>
        <div class="timeline-axis">
          ${lt(r).map(m=>`<span>${m}ms</span>`).join("")}
        </div>
        <div class="timeline-lanes">${a}</div>
      </div>
    </section>
  `,(l=e.querySelector("#timeline-prev"))==null||l.addEventListener("click",()=>{q=Math.max(0,i-1),re(e,t)}),(u=e.querySelector("#timeline-next"))==null||u.addEventListener("click",()=>{q=Math.min(t.dispatches.length-1,i+1),re(e,t)})}function at(e,t){return t>=0&&t<e.length?e[t]:[...e].sort((n,s)=>s.durationNanos-n.durationNanos)[0]}function rt(e){const t=[];return e.blockMaterial&&t.push(e.blockMaterial),e.blockX!=null&&e.blockY!=null&&e.blockZ!=null&&t.push(`@ ${e.blockX},${e.blockY},${e.blockZ}`),t.join(" ")}function lt(e){const t=e<=12?2:Math.ceil(e/6),n=[];for(let s=0;s<=e;s+=t)n.push(s);return n[n.length-1]!==e&&n.push(e),n}function ct(e,t){const n=W(e),s=new Map;for(const o of n){const c=pt(o.priority),d=s.get(c)??[];d.push(o),s.set(c,d)}let i=0;const r=[];for(const o of ot){const c=s.get(o);if(!(c!=null&&c.length))continue;c.sort((u,m)=>u.invocationOrder-m.invocationOrder);const d=o==="MONITOR",a=c.map(u=>{const m=i,f=u.durationNanos/1e6;i+=f;const y=m/t*100,h=Math.max(d?.6:.8,f/t*100),M=ut(u,f),T=d?"monitor":M,Wt=ft(u.pluginName),Bt=u.exceedsSlowThreshold?" · flagged":"",Ut=d?"":`<span>${v(Wt)} ${N(u.durationNanos)}${Bt}</span>`;return`
          <div class="timeline-bar bar-${T}" style="left:${y.toFixed(2)}%;width:${h.toFixed(2)}%;" title="${v(u.pluginName)}.${v(u.methodName)} · ${N(u.durationNanos)}">
            ${Ut}
          </div>`}).join(""),l=d&&c.length?`<div class="lane-note">${c.map(u=>`${v(u.pluginName)} ${N(u.durationNanos)}`).join(", ")} (read-only observers)</div>`:"";r.push(`
      <div class="timeline-lane-block">
        <div class="lane-label">${dt(o)}</div>
        <div class="lane-track">${a}</div>
        ${l}
      </div>`)}return r.length?r.join(""):`<div class="empty">${e.listenerTimings.length>0?"No listeners matched the timeline filters.":"No per-listener timings for this dispatch. Attach the EventLens agent for measured timings, or wait for the full trace sync."}</div>`}function dt(e){return e==="LOWEST"?"Lowest":e==="LOW"?"Low":e==="NORMAL"?"Normal":e==="HIGH"||e==="HIGHEST"?"High":"Monitor"}function ut(e,t){return e.exceedsSlowThreshold||t>=5?"critical":t>=2?"warn":"ok"}function pt(e){const t=e.toUpperCase();return t.includes("MONITOR")?"MONITOR":t.includes("LOWEST")?"LOWEST":t.includes("LOW")?"LOW":t.includes("HIGHEST")?"HIGHEST":t.includes("HIGH")?"HIGH":"NORMAL"}function ft(e){return e.length<=14?e:e.slice(0,12)+"…"}function vt(e,t){const n=R(t.session.state),s=ht(t.dispatches);if(!s){e.innerHTML=`<section class="page"><header class="page-header"><h1>Flame graph</h1></header>${n}<p class="empty">No dispatches captured.</p></section>`;return}if(!W(s).length){e.innerHTML=`<section class="page"><header class="page-header"><h1>Flame graph</h1></header>${n}<p class="empty">No listener timing data. Attach the EventLens agent for flame view, or wait for the full trace sync.</p></section>`;return}const i=O(s.eventClassName),r=W(s).sort((l,u)=>l.invocationOrder-u.invocationOrder),o=[...r].sort((l,u)=>u.durationNanos-l.durationNanos)[0],c=mt(r,s.durationNanos),d=gt(r,s.durationNanos,o),a=$t(o);e.innerHTML=`
    <section class="page">
      <header class="page-header">
        <h1>Flame graph</h1>
        <p class="page-subtitle mono">Call stack for the same trace · widths scaled to ${N(s.durationNanos)}</p>
      </header>
      ${n}
      <div class="flame-panel">
        <div class="flame-stack">
          <div class="flame-row">
            <div class="flame-block block-root">
              <span>${v(i)} · ${N(s.durationNanos)} · 100%</span>
            </div>
          </div>
          <div class="flame-row">${c}</div>
          ${d?`<div class="flame-row">${d}</div>`:""}
        </div>
        ${a?`<div class="flame-insight">${a}</div>`:""}
      </div>
    </section>
  `}function ht(e){return e.length?[...e].sort((t,n)=>n.durationNanos-t.durationNanos)[0]:null}function mt(e,t){let n=0;return e.map(s=>{const i=Math.max(.2,s.durationNanos/t*100),r=n;n+=i;const o=s.priority.toUpperCase().includes("MONITOR"),c=bt(s,s.durationNanos/t),d=o?"monitor":c,a=o||i<3?"":`<span>${v(s.pluginName)}${i>=8?` · ${N(s.durationNanos)}`:""}</span>`;return`
        <div class="flame-block block-${d}" style="left:${r.toFixed(2)}%;width:${i.toFixed(2)}%;" title="${v(s.pluginName)}.${v(s.methodName)}">
          ${a}
        </div>`}).join("")}function gt(e,t,n){if(!n||n.durationNanos/t<.2)return"";const i=e.slice(0,e.indexOf(n)).reduce((a,l)=>a+l.durationNanos,0)/t*100,r=n.durationNanos/t*100,o=yt(n);if(o.length>1){let a=i;return o.map(l=>{const u=l.nanos/t*100,m=`
          <div class="flame-block block-sub block-sub-crit" style="left:${a.toFixed(2)}%;width:${Math.max(u,2).toFixed(2)}%;">
            <span>${v(l.label)} ${N(l.nanos)}</span>
          </div>`;return a+=u,m}).join("")}const c=e.filter(a=>a!==n&&a.durationNanos>=t*.03);if(!c.length)return`
      <div class="flame-block block-sub block-sub-crit" style="left:${i.toFixed(2)}%;width:${Math.max(r*.4,8).toFixed(2)}%;">
        <span>${v(n.methodName)} · ${N(n.durationNanos)}</span>
      </div>`;let d=i;return c.slice(0,4).map(a=>{const l=a.durationNanos/t*100,m=`
        <div class="flame-block block-sub block-sub-${a.exceedsSlowThreshold?"crit":"ok"}" style="left:${d.toFixed(2)}%;width:${Math.max(l,2).toFixed(2)}%;">
          <span>${v(a.methodName)} · ${N(a.durationNanos)}</span>
        </div>`;return d+=l,m}).join("")}function yt(e){const t=e.methodName.split(/(?=[A-Z])|_/).filter(Boolean);if(t.length<=1)return[];const n=Math.ceil(e.durationNanos/t.length);return t.map((s,i)=>({label:s,nanos:i===t.length-1?e.durationNanos-n*(t.length-1):n}))}function bt(e,t){return e.exceedsSlowThreshold||t>.4?"critical":t>.15?"warn":"ok"}function $t(e){if(!e)return"";const t=oe(e.durationNanos);return`<div><span class="${e.exceedsSlowThreshold?"severity-crit":"severity-warn"}">${v(e.pluginName)}'s ${N(e.durationNanos)}</span> (${t} tick) is the dominant cost in this dispatch.</div>`}function Nt(e,t,n){const s=n?O(n.session.eventClassName):null,i=St(t,s);if(!i.nodes.length){e.innerHTML='<section class="page"><p class="empty">No event graph data available.</p></section>';return}const r=960,o=420,c=i.edges.map(l=>{const u=i.nodes.find(T=>T.id===l.from),m=i.nodes.find(T=>T.id===l.to);if(!u||!m)return"";const f=u.x+70,y=u.y+30,h=m.x+70,M=m.y+30;return`<line class="flow-edge${l.primary?" primary":""}" x1="${f}" y1="${y}" x2="${h}" y2="${M}" />`}).join(""),d=i.nodes.map(l=>`
      <div class="flow-node${l.primary?" primary":""}" style="left:${l.x}px;top:${l.y}px">
        <div class="flow-node-title">${v(l.label)}</div>
        <div class="flow-node-sub">${v(l.sublabel)}</div>
      </div>`).join(""),a=s?`Cascades observed downstream of ${s} this session. Node size = frequency.`:"Event and plugin relationships across active trace sessions.";e.innerHTML=`
    <section class="page">
      <header class="page-header">
        <h1>Event graph</h1>
        <p class="page-subtitle">${a}</p>
      </header>
      ${n?R(n.session.state):""}
      <div class="flow-panel">
        <div class="flow-canvas" style="width:${r}px;height:${o}px">
          <svg class="flow-edges" width="${r}" height="${o}">${c}</svg>
          ${d}
        </div>
      </div>
    </section>
  `}function St(e,t){const n=e.nodes.filter(a=>a.kind==="EVENT"),s=e.nodes.filter(a=>a.kind==="PLUGIN");if(!n.length&&!s.length)return{nodes:[],edges:[]};const i=n.find(a=>t&&a.label===t)??n.sort((a,l)=>l.weight-a.weight)[0]??null,r=n.filter(a=>a!==i).sort((a,l)=>l.weight-a.weight).slice(0,3),o=n.filter(a=>a!==i&&!r.includes(a)).sort((a,l)=>l.weight-a.weight).slice(0,1),c=[],d=[];if(i&&c.push({id:i.id,label:i.label,sublabel:`${i.weight.toLocaleString()} / session`,primary:!0,x:20,y:190}),r.forEach((a,l)=>{const u=[60,190,320];c.push({id:a.id,label:a.label,sublabel:`${a.weight.toLocaleString()} observed`,primary:l===0&&o.length>0,x:270,y:u[l]??190}),i&&d.push({from:i.id,to:a.id,primary:l===0})}),o.length&&r[0]){c.push({id:o[0].id,label:o[0].label,sublabel:`${o[0].weight.toLocaleString()} observed`,primary:!0,x:530,y:60}),d.push({from:r[0].id,to:o[0].id,primary:!0});const a=n.find(l=>!c.some(u=>u.id===l.id));a&&(c.push({id:a.id,label:a.label,sublabel:`${a.weight.toLocaleString()} observed`,primary:!1,x:790,y:60}),d.push({from:o[0].id,to:a.id,primary:!0}))}return r.length||s.sort((a,l)=>l.weight-a.weight).slice(0,4).forEach((a,l)=>{const u=[60,190,320,190];c.push({id:a.id,label:a.label,sublabel:`${a.weight.toLocaleString()} invocations`,primary:l===0,x:270+Math.floor(l/2)*260,y:u[l]??190}),i&&d.push({from:i.id,to:a.id,primary:l===0})}),{nodes:c,edges:d}}const le=["LOWEST","LOW","NORMAL","HIGH","MONITOR"];function wt(e,t){if(!(t!=null&&t.dispatches.length)){const o=t?R(t.session.state):"";e.innerHTML=`${o?`<section class="page"><header class="page-header"><h1>Plugin graph</h1></header>${o}<p class="empty">No dispatch data for plugin graph.</p></section>`:'<section class="page"><p class="empty">No dispatch data for plugin graph.</p></section>'}`;return}const n=[...t.dispatches].sort((o,c)=>c.durationNanos-o.durationNanos)[0],s=Tt(n),r=le.map(o=>{const c=s.filter(d=>d.priority===o||o==="HIGH"&&d.priority==="HIGHEST");return`
      <div class="priority-column">
        <div class="priority-label">${Lt(o)}</div>
        ${c.length?c.map(d=>`
              <div class="plugin-card card-${d.status}">
                <div class="plugin-name">${v(d.plugin)}</div>
                <div class="plugin-time">${N(d.durationNanos)}</div>
                <div class="plugin-tick">${oe(d.durationNanos)} tick${d.status==="critical"?" · flagged":""}${d.readOnly&&d.status!=="critical"?" · read-only":""}</div>
              </div>`).join(""):""}
      </div>`}).map((o,c)=>c===0?o:`<div class="priority-arrow">›</div>${o}`).join("");e.innerHTML=`
    <section class="page">
      <header class="page-header">
        <h1>Plugin graph</h1>
        <p class="page-subtitle">Execution order for this trace, grouped by listener priority. Node color = severity.</p>
      </header>
      ${R(t.session.state)}
      <div class="plugin-panel">
        <div class="priority-grid">${r}</div>
      </div>
    </section>
  `}function Lt(e){return e==="LOWEST"?"Lowest":e==="LOW"?"Low":e==="NORMAL"?"Normal":e==="HIGH"?"High":"Monitor"}function Tt(e){const t=[];for(const n of W(e)){const s=kt(n.priority),i=n.durationNanos/5e7*100,r=s==="MONITOR",o=r?"monitor":n.exceedsSlowThreshold||i>=10?"critical":i>=3?"warn":"ok",c=r||n.pluginName.toLowerCase().includes("eventlens");t.push({plugin:n.pluginName,priority:s,durationNanos:n.durationNanos,status:o,readOnly:c})}return t.sort((n,s)=>{const i=le.indexOf(n.priority==="HIGHEST"?"HIGH":n.priority)-le.indexOf(s.priority==="HIGHEST"?"HIGH":s.priority);return i!==0?i:s.durationNanos-n.durationNanos})}function kt(e){const t=e.toUpperCase();return t.includes("MONITOR")?"MONITOR":t.includes("LOWEST")?"LOWEST":t.includes("LOW")?"LOW":t.includes("HIGHEST")?"HIGHEST":t.includes("HIGH")?"HIGH":"NORMAL"}function Mt(e,t){const n=[{label:"Event",left:e.session.eventClassName,right:t.session.eventClassName},{label:"Captured",left:String(e.session.capturedEvents),right:String(t.session.capturedEvents)},{label:"Dispatches",left:String(e.dispatches.length),right:String(t.dispatches.length)}],s=Ct(e,t);return n.push({label:"Correlated pairs",left:String(s),right:String(s)}),n}function Ct(e,t){const n=new Set(t.dispatches.map(s=>s.correlationKey).filter(s=>!!s));return e.dispatches.filter(s=>s.correlationKey&&n.has(s.correlationKey)).length}function It(e,t,n,s){const i=`<label class="file-load">Right report
      <input type="file" accept="application/json,.json" /></label>`;if(!t||!n){e.innerHTML=`<section class="page"><header class="page-header"><h1>Compare</h1></header>
      <p class="empty">Load a left report (or keep the current session) and choose a second JSON to compare.</p>
      ${i}</section>`,ke(e,s);return}const r=Mt(t,n).map(o=>`<tr><th>${v(o.label)}</th><td>${v(o.left)}</td><td>${v(o.right)}</td></tr>`).join("");e.innerHTML=`<section class="page"><header class="page-header"><h1>Compare</h1></header>
    ${i}
    <table class="data-table"><thead><tr><th></th><th>Left</th><th>Right</th></tr></thead><tbody>${r}</tbody></table>
    </section>`,ke(e,s)}function ke(e,t){const n=e.querySelector('input[type="file"]');!n||!t||n.addEventListener("change",()=>{var i;const s=(i=n.files)==null?void 0:i[0];s&&t(s)})}const g=document.querySelector("#app");let p=null,Me=null,L="overview",b="",ce="",S="offline",F=!1,_=!1,Y=null,de=!1,ue=0,K="Paper —",$=null,Ce=!1,j=null,X=null,Z=null,pe=[];function w(){return b||(p==null?void 0:p.session.sessionId)||""}function I(e){const t=e.environment;if(t){const n=t.runtimeKind??"paper";if(n==="paper")K=t.paperVersion??t.platformLabel??"Paper server";else{const s=t.loaderVersion??t.platformLabel??n;K=`${n} client · ${s}`}}e.instrumentation&&(de=e.instrumentation.agentPresent,ue=e.instrumentation.protocolVersion)}function Et(e){$=e,de=e.agentPresent,ue=e.protocolVersion,e.paperVersion&&(K=e.paperVersion),!b&&!F&&e.activeTraceSessionId&&S!=="report"&&(b=e.activeTraceSessionId)}function z(){var t;const e=w();return e?((p==null?void 0:p.session.sessionId)===e?p.session.state:null)??((t=pe.find(n=>n.sessionId===e))==null?void 0:t.state)??null:(p==null?void 0:p.session.state)??null}function E(){return{activeView:L,liveMode:A(),streamConnected:_,agentPresent:de,protocolVersion:ue,paperVersion:K,serverStatus:$,report:p,sessionState:z(),selectedSessionId:b,onNavigate:e=>{L=e,fe(e),k()},onSessionChange:e=>{he(e,!0)},onReportChange:e=>{Ie(e)},onFileLoad:e=>{Rt(e)}}}function D(){Ce||(Ke(g,E()),Ce=!0)}function fe(e){g.querySelectorAll(".nav-item[data-view]").forEach(t=>{t.classList.toggle("active",t.dataset.view===e)})}function ve(e){const t=Fe(e);if(!t)return!1;const n=w();return n&&n!==t.sessionId?!1:(b||(b=t.sessionId,S="session",B(g,t.sessionId,"")),p=Ve(p,t),x(g,E()),ie(g,t.sessionId,t.capturedEvents,t.eventClassName,t.state),k(!1),xt(),!0)}function xt(){j!==null&&window.clearTimeout(j),j=window.setTimeout(()=>{j=null,Ht()},400)}async function Ot(){if(!(S!=="session"||!w()))try{const e=await G(w());if(w()!==e.session.sessionId)return;p=e,I(e),x(g,E()),ie(g,e.session.sessionId,e.session.capturedEvents,e.session.eventClassName,e.session.state)}catch{}}function At(){return p!=null&&p.dispatches.length?p.dispatches.some(e=>{var t;return e.listenerTimings.length===0&&(((t=e.listenerChain)==null?void 0:t.length)??0)===0}):!0}async function Ht(){if(!(S!=="session"||!w()))try{const e=await G(w());if(w()!==e.session.sessionId)return;p=e,I(e),x(g,E()),ie(g,e.session.sessionId,e.session.capturedEvents,e.session.eventClassName,e.session.state),k(!1)}catch{}}async function J(){const[e,t]=await Promise.all([ye(),be()]);return pe=e,Ze(g,e,t,b,ce,z()),e}async function he(e,t){b=e,ce="",S="session",F=t,ae(),B(g,e,""),p=await G(e),I(p),x(g,E()),await k(!1)}async function Ie(e){ce=e,b="",S="report",F=!1,ae(),B(g,"",e),p=await Re(e),I(p),x(g,E()),await k(!1)}async function Pt(){if(A())return;const e=window.__EVENTLENS_REPORT__;if(e&&typeof e=="object"){p=e,S="offline",I(p),D(),await k(!1);return}const t=new URLSearchParams(window.location.search).get("report"),n=t&&t.length>0?t:"./report.json";try{const s=await fetch(n);if(!s.ok)return;p=ee(await s.text()),S="offline",I(p),D(),await k(!1)}catch{}}async function Rt(e){S="offline",F=!1,ae(),p=ee(await e.text()),I(p),L="overview",D(),fe(L),await k(!1)}async function me(e){const t=e.filter(s=>s.state==="ACTIVE");if(!t.length||b!==""&&t.some(s=>s.sessionId===b))return!1;if(!b||!F||S!=="session"){const s=t[0];return await he(s.sessionId,!1),!0}return!1}async function Ee(){S!=="session"||!w()||(p=await G(w()),I(p),x(g,E()),await k(!1))}async function ge(){if(A())try{const e=await Pe();Et(e),x(g,E())}catch{}}function qt(){const e=z();if(p&&e&&$e(e))return p.session.startedAtMillis;if($!=null&&$.activeTraceSessionId&&$.activeTraceStartedAtMillis>0){const n=$.activeTraceSessionId;if(!w()||w()===n)return $.activeTraceStartedAtMillis}const t=pe.find(n=>n.sessionId===w());return t&&$e(t.state)?t.startedAtMillis:null}function Ft(){const e=qt();if(e==null)return;const t=Math.max(0,Date.now()-e),n=g.querySelector("#view-root");n&&L==="overview"&&it(n,t)}function jt(){Z===null&&(Z=window.setInterval(Ft,1e3))}function Dt(){X===null&&(X=window.setInterval(()=>{ge()},2e3))}async function xe(){if(!A())return;await ge();const e=await J();await me(e)||S==="session"&&b&&await Ee()}function Gt(e,t){if(e==="session-started"){if(ve(t))return;(async()=>{await J();const n=typeof t.sessionId=="string"?t.sessionId:"";n&&(!w()||!F)?await he(n,!1):await me(await ye())})();return}if(e==="dispatch"){if(ve(t))return;Ee();return}if(e==="poll"){xe();return}if(e==="session-stopped"){if(ve(t)){J();return}xe()}}async function Vt(){if(D(),!A())return;await ge(),x(g,E()),Dt(),jt();const e=await J(),t=await be();if(e.length){if(await me(e),!b&&($!=null&&$.activeTraceSessionId)&&(b=$.activeTraceSessionId,S="session"),b&&!p)try{p=await G(b),I(p),B(g,b,"")}catch{p=null}}else t.length&&t[0].format==="json"&&await Ie(t[0].fileName);const n=_e((i,r)=>{Gt(i,r)});Y=n.stop;const s=()=>{const i=n.connected();i!==_&&(_=i,Xe(g,_,z()))};s(),window.setInterval(s,1e3)}function k(e=!0){D();const t=g.querySelector("#view-root");return t?(fe(L),e&&A()&&S==="session"&&!p?(t.innerHTML='<p class="empty">Refreshing live trace data…</p>',Promise.resolve()):(A()&&S==="session"&&At()?Ot():Promise.resolve()).then(()=>{if(L==="events"){qe().then(s=>Nt(t,s,p)).catch(()=>{t.innerHTML='<section class="page"><p class="empty">Failed to load event graph.</p></section>'});return}if(L==="plugins"){wt(t,p);return}if(!p){if($!=null&&$.activeTraceSessionId){const s=$.activeTraceStartedAtMillis>0?Math.max(0,Date.now()-$.activeTraceStartedAtMillis):0;t.innerHTML=`
          <section class="page">
            <header class="page-header">
              <h1>Overview</h1>
              <p class="page-subtitle">Trace session is active. Waiting for first dispatch…</p>
            </header>
            <div class="stat-grid">
              <div class="stat-card">
                <div class="stat-label">Session uptime</div>
                <div class="stat-value" id="stat-session-uptime">${Q(s)}</div>
              </div>
            </div>
            <p class="empty">Interact in-game to capture events, or wait for the live feed to populate.</p>
          </section>`}else t.innerHTML='<section class="page"><p class="empty">No active trace session. Start one with <span class="mono">/eventlens trace start &lt;Event&gt;</span>.</p></section>';return}L==="overview"?nt(t,p):L==="timeline"?re(t,p):L==="flame"?vt(t,p):L==="compare"&&It(t,p,Me,s=>{s.text().then(i=>{Me=ee(i),k(!1)})})})):Promise.resolve()}D(),Pt(),Vt().then(()=>k(!1)).catch(e=>{const t=g.querySelector("#view-root");t&&(t.innerHTML=`<p class="empty">Failed to load dashboard: ${e}</p>`)}),window.addEventListener("beforeunload",()=>{Y==null||Y(),j!==null&&window.clearTimeout(j),X!==null&&window.clearInterval(X),Z!==null&&window.clearInterval(Z)})})();
