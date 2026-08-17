(function(){const t=document.createElement("link").relList;if(t&&t.supports&&t.supports("modulepreload"))return;for(const i of document.querySelectorAll('link[rel="modulepreload"]'))n(i);new MutationObserver(i=>{for(const a of i)if(a.type==="childList")for(const o of a.addedNodes)o.tagName==="LINK"&&o.rel==="modulepreload"&&n(o)}).observe(document,{childList:!0,subtree:!0});function s(i){const a={};return i.integrity&&(a.integrity=i.integrity),i.referrerPolicy&&(a.referrerPolicy=i.referrerPolicy),i.crossOrigin==="use-credentials"?a.credentials="include":i.crossOrigin==="anonymous"?a.credentials="omit":a.credentials="same-origin",a}function n(i){if(i.ep)return;i.ep=!0;const a=s(i);fetch(i.href,a)}})();function $(e){return`${(e/1e6).toFixed(2)}ms`}function E(e){const t=e.lastIndexOf(".");return t>=0?e.substring(t+1):e}function se(e){const t=Math.floor(e/1e3),s=Math.floor(t/3600),n=Math.floor(t%3600/60),i=t%60;return s>0?`${s}h ${n}m ${i}s`:n>0?`${n}m ${i}s`:`${i}s`}function h(e){return e.replaceAll("&","&amp;").replaceAll("<","&lt;").replaceAll(">","&gt;").replaceAll('"',"&quot;")}function xe(e){return e.toLocaleTimeString([],{hour:"2-digit",minute:"2-digit",second:"2-digit",hour12:!1})}function Ae(e){const t=new Date(e),s=xe(t),n=String(t.getMilliseconds()).padStart(3,"0");return`${s}.${n}`}const He=()=>window.location.protocol.startsWith("http")&&window.location.port!=="";async function Pe(){return(await fetch("/api/status")).json()}async function Ne(){return(await(await fetch("/api/sessions")).json()).sessions??[]}async function $e(){return(await(await fetch("/api/reports")).json()).reports??[]}async function V(e){const t=await fetch(`/api/sessions/${encodeURIComponent(e)}/report`);if(!t.ok)throw new Error("Session report unavailable");return t.json()}async function Re(e){const t=await fetch(`/api/reports/${encodeURIComponent(e)}`);if(!t.ok)throw new Error("Report file unavailable");return t.json()}async function qe(){return(await fetch("/api/graph/events")).json()}function P(){return He()}function Fe(e){return JSON.parse(e)}function De(e){if(typeof e.sessionId!="string")return null;const t={sessionId:e.sessionId,eventClassName:typeof e.eventClassName=="string"?e.eventClassName:"",state:typeof e.state=="string"?e.state:"ACTIVE",capturedEvents:typeof e.capturedEvents=="number"?e.capturedEvents:0,durationMillis:typeof e.durationMillis=="number"?e.durationMillis:0,startedAtMillis:typeof e.startedAtMillis=="number"?e.startedAtMillis:Date.now()};return e.dispatch&&typeof e.dispatch=="object"&&(t.dispatch=je(e.dispatch)),t}function je(e){const t=Array.isArray(e.listenerTimings)?e.listenerTimings.filter(n=>typeof n=="object"&&n!==null).map(Ve):[],s=Array.isArray(e.listenerChain)?e.listenerChain.filter(n=>typeof n=="object"&&n!==null).map(Ge):void 0;return{sequence:typeof e.sequence=="number"?e.sequence:0,startedAtMillis:typeof e.startedAtMillis=="number"?e.startedAtMillis:0,durationNanos:typeof e.durationNanos=="number"?e.durationNanos:0,durationMillis:typeof e.durationMillis=="string"?e.durationMillis:"0ms",eventLensOverheadNanos:typeof e.eventLensOverheadNanos=="number"?e.eventLensOverheadNanos:0,eventClassName:typeof e.eventClassName=="string"?e.eventClassName:"",cancelledAtStart:e.cancelledAtStart===!0,cancelledAtEnd:e.cancelledAtEnd===!0,playerName:typeof e.playerName=="string"?e.playerName:null,worldName:typeof e.worldName=="string"?e.worldName:null,blockX:typeof e.blockX=="number"?e.blockX:null,blockY:typeof e.blockY=="number"?e.blockY:null,blockZ:typeof e.blockZ=="number"?e.blockZ:null,listenerChain:s,listenerTimings:t}}function Ge(e){return{registrationOrder:typeof e.registrationOrder=="number"?e.registrationOrder:void 0,pluginName:typeof e.pluginName=="string"?e.pluginName:"",listenerClassName:typeof e.listenerClassName=="string"?e.listenerClassName:"",methodName:typeof e.methodName=="string"?e.methodName:"",priority:typeof e.priority=="string"?e.priority:"NORMAL"}}function Ve(e){return{invocationOrder:typeof e.invocationOrder=="number"?e.invocationOrder:0,pluginName:typeof e.pluginName=="string"?e.pluginName:"",listenerClassName:typeof e.listenerClassName=="string"?e.listenerClassName:"",methodName:typeof e.methodName=="string"?e.methodName:"",priority:typeof e.priority=="string"?e.priority:"NORMAL",durationNanos:typeof e.durationNanos=="number"?e.durationNanos:0,durationMillis:typeof e.durationMillis=="string"?e.durationMillis:typeof e.durationMillis=="number"?`${e.durationMillis}ms`:"0ms",exceedsSlowThreshold:e.exceedsSlowThreshold===!0,threwException:e.threwException===!0,exceptionType:typeof e.exceptionType=="string"?e.exceptionType:null}}function We(e,t){const s=t.dispatch,n=e&&e.session.sessionId===t.sessionId?e:Ue(t),i=[...n.dispatches];if(s){const o=i.findIndex(c=>c.sequence===s.sequence);o>=0?i[o]=Be(i[o],s):i.push(s)}const a=Math.max(t.capturedEvents,i.length);return{...n,session:{...n.session,sessionId:t.sessionId,eventClassName:t.eventClassName||n.session.eventClassName,state:t.state,capturedEvents:a,durationMillis:t.durationMillis,startedAtMillis:t.startedAtMillis},dispatches:i}}function Be(e,t){const s=t.listenerTimings.length>0?t.listenerTimings:e.listenerTimings,n=t.listenerChain&&t.listenerChain.length>0?t.listenerChain:e.listenerChain;return{...t,listenerTimings:s,listenerChain:n}}function Ue(e){return{reportVersion:"live",redactionMode:"live",session:{sessionId:e.sessionId,eventClassName:e.eventClassName,state:e.state,ownerName:"",startedAtMillis:e.startedAtMillis,durationMillis:e.durationMillis,capturedEvents:e.capturedEvents,droppedEvents:0,sampledOutEvents:0,filters:""},warnings:[],dispatches:e.dispatch?[e.dispatch]:[]}}const Ye=2e3;function Xe(e){let t=!0,s=!1,n=null,i=null;const a=()=>{s=!0,i!==null&&(window.clearInterval(i),i=null)},o=()=>{!t||i!==null||(i=window.setInterval(()=>{e("poll",{})},Ye))},c=()=>{!t||n||(n=new EventSource("/api/stream"),n.onopen=()=>{a()},n.addEventListener("connected",()=>{a()}),n.addEventListener("dispatch",d=>{K(e,"dispatch",d.data)}),n.addEventListener("session-started",d=>{K(e,"session-started",d.data)}),n.addEventListener("session-stopped",d=>{K(e,"session-stopped",d.data)}),n.onerror=()=>{s=!1,n==null||n.close(),n=null,o(),t&&window.setTimeout(c,1e3)})};return c(),{stop:()=>{t=!1,s=!1,n==null||n.close(),n=null,i!==null&&window.clearInterval(i)},connected:()=>s}}function K(e,t,s){try{e(t,JSON.parse(s))}catch{e(t,{})}}const D={overview:'<svg width="16" height="16" viewBox="0 0 16 16"><rect x="1" y="1" width="6" height="6" rx="1.2" fill="currentColor"/><rect x="9" y="1" width="6" height="6" rx="1.2" fill="currentColor"/><rect x="1" y="9" width="6" height="6" rx="1.2" fill="currentColor"/><rect x="9" y="9" width="6" height="6" rx="1.2" fill="currentColor"/></svg>',timeline:'<svg width="16" height="16" viewBox="0 0 16 16"><line x1="1" y1="4" x2="15" y2="4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/><line x1="1" y1="8" x2="10" y2="8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/><line x1="1" y1="12" x2="13" y2="12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>',flame:'<svg width="16" height="16" viewBox="0 0 16 16"><rect x="1" y="10" width="3" height="5" fill="currentColor"/><rect x="6.5" y="6" width="3" height="9" fill="currentColor"/><rect x="12" y="2" width="3" height="13" fill="currentColor"/></svg>',events:'<svg width="16" height="16" viewBox="0 0 16 16"><line x1="3" y1="13" x2="8" y2="3" stroke="currentColor" stroke-width="1.3"/><line x1="13" y1="13" x2="8" y2="3" stroke="currentColor" stroke-width="1.3"/><line x1="3" y1="13" x2="13" y2="13" stroke="currentColor" stroke-width="1.3"/><circle cx="3" cy="13" r="1.8" fill="currentColor"/><circle cx="13" cy="13" r="1.8" fill="currentColor"/><circle cx="8" cy="3" r="1.8" fill="currentColor"/></svg>',plugins:'<svg width="16" height="16" viewBox="0 0 16 16"><line x1="2" y1="8" x2="8" y2="3" stroke="currentColor" stroke-width="1.3"/><line x1="2" y1="8" x2="8" y2="13" stroke="currentColor" stroke-width="1.3"/><line x1="8" y1="3" x2="14" y2="8" stroke="currentColor" stroke-width="1.3"/><line x1="8" y1="13" x2="14" y2="8" stroke="currentColor" stroke-width="1.3"/><circle cx="2" cy="8" r="1.6" fill="currentColor"/><circle cx="8" cy="3" r="1.6" fill="currentColor"/><circle cx="8" cy="13" r="1.6" fill="currentColor"/><circle cx="14" cy="8" r="1.6" fill="currentColor"/></svg>'};function ge(e){return e==="ACTIVE"||e==="THROTTLED"}function q(e){switch(e){case"ACTIVE":return{label:"Active",shortLabel:"ACTIVE",tone:"active",isActive:!0};case"THROTTLED":return{label:"Throttled",shortLabel:"THROTTLED",tone:"warn",isActive:!0};case"STOPPED":return{label:"Stopped",shortLabel:"STOPPED",tone:"stopped",isActive:!1};case"FULL":return{label:"Full",shortLabel:"FULL",tone:"stopped",isActive:!1};case"EXPIRED":return{label:"Expired",shortLabel:"EXPIRED",tone:"stopped",isActive:!1};case"ABANDONED":return{label:"Abandoned",shortLabel:"ABANDONED",tone:"stopped",isActive:!1};default:return{label:e,shortLabel:e,tone:"stopped",isActive:!1}}}function Se(e){const t=q(e).tone;return t==="active"?"session-option-active":t==="warn"?"session-option-warn":"session-option-stopped"}function ne(e){if(!e)return"";const t=q(e).tone;return t==="active"?"session-select-active":t==="warn"?"session-select-warn":"session-select-stopped"}function we(e,t,s,n){const i=q(n),a=E(t);return`${e} · ${a} (${s}) · ${i.shortLabel}`}function R(e){const t=q(e);return t.tone!=="stopped"?"":`
    <div class="page-notice page-notice-stopped" role="status">
      <span class="page-notice-dot"></span>
      <span class="page-notice-label">${h(t.shortLabel)}</span>
      <span class="page-notice-text">${h("This trace has ended. You are viewing a frozen snapshot — data will not update.")}</span>
    </div>`}function ie(e,t){const s=t?"live stream":"refreshes every 2s",n=e?q(e):null;return!n||n.isActive?`
      <div id="trace-status-indicator" class="trace-status-group trace-status-live">
        <span class="trace-status-dot"></span>
        <span class="trace-status-label">LIVE</span>
        ${e==="THROTTLED"?'<span class="trace-throttle-pill">throttled</span>':""}
        <span class="status-muted">· ${h(s)}</span>
      </div>`:`
    <div id="trace-status-indicator" class="trace-status-group trace-status-stopped">
      <span class="trace-status-dot"></span>
      <span class="trace-status-label">${h(n.shortLabel)}</span>
      <span class="status-muted">· trace ended</span>
    </div>`}const _e=[{id:"overview",label:"Overview",icon:D.overview},{id:"timeline",label:"Timeline",icon:D.timeline},{id:"flame",label:"Flame graph",icon:D.flame},{id:"events",label:"Event graph",icon:D.events},{id:"plugins",label:"Plugin graph",icon:D.plugins}];function Ze(e,t){var r,l,u,m;const s=Le(t),n=t.sessionState,i=Te(t),a=t.agentPresent?`agent v${t.protocolVersion}`:"agent absent",o=(r=t.serverStatus)==null?void 0:r.tps,c=o!=null?o.toFixed(1):"—",d=o!=null&&o>=18?"tps-ok":"status-highlight";e.className="app-shell",e.innerHTML=`
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-mark">EL</div>
        <div class="brand-text">
          <span class="brand-name">EventLens</span>
          <span class="brand-sub">DIAGNOSTICS</span>
        </div>
      </div>
      <nav class="sidebar-nav">
        ${_e.map(f=>`
          <button type="button" class="nav-item${t.activeView===f.id?" active":""}" data-view="${f.id}">
            <span class="nav-icon">${f.icon}</span>
            <span>${f.label}</span>
          </button>`).join("")}
      </nav>
      <div class="sidebar-footer">
        <span>${j(t.paperVersion)}</span><br>
        <span>${j(a)}</span>
      </div>
    </aside>
    <div class="main-column">
      <header class="status-bar">
        <div class="status-left">
          ${t.liveMode?`${ie(n,t.streamConnected)}<div class="status-divider"></div>`:'<span class="status-muted">Offline report</span><div class="status-divider"></div>'}
          <div class="status-mono">session <span class="status-highlight">${k(s)}</span></div>
          <div class="status-mono">world <span class="status-highlight">${k(i)}</span></div>
        </div>
        <div class="status-right">
          <div class="status-mono">TPS <span id="status-tps" class="${d}">${k(c)}</span></div>
          <div class="status-mono">tick budget <span class="status-highlight">50ms</span></div>
          <span class="agent-pill${t.agentPresent?"":" absent"}">${k(a)}</span>
        </div>
      </header>
      ${t.liveMode?`<div class="source-bar">
              <select id="session-select" class="source-select ${ne(n)}">
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
  `,e.querySelectorAll(".nav-item[data-view]").forEach(f=>{f.addEventListener("click",()=>{t.onNavigate(f.dataset.view)})}),t.liveMode?((l=e.querySelector("#session-select"))==null||l.addEventListener("change",f=>{const y=f.target.value;y&&t.onSessionChange(y)}),(u=e.querySelector("#report-select"))==null||u.addEventListener("change",f=>{const y=f.target.value;y&&t.onReportChange(y)})):(m=e.querySelector("#file-input"))==null||m.addEventListener("change",async f=>{var T;const v=(T=f.target.files)==null?void 0:T[0];v&&t.onFileLoad(v)})}function oe(e,t,s,n,i){const a=e.querySelector("#session-select");if(a)for(const o of a.options){if(o.value!==t)continue;const c=i??o.dataset.state??"ACTIVE";o.className=Se(c),o.dataset.state=c,o.textContent=we(t,n,s,c);break}}function _(e,t,s){const n=e.querySelector("#session-select"),i=e.querySelector("#report-select");n&&t&&(n.value=t),i&&s?i.value=s:i&&t&&(i.value="")}function ze(e,t,s){const n=e.querySelector("#trace-status-indicator");if(n){n.outerHTML=ie(s,t);return}const i=e.querySelector(".trace-status-group .status-muted");i&&(i.textContent=s&&!q(s).isActive?"· trace ended":`· ${t?"live stream":"refreshes every 2s"}`)}function Je(e,t,s,n,i,a){var y;const o=e.querySelector("#session-select"),c=e.querySelector("#report-select");if(!o||!c)return;const d=o.value,r=c.value;o.innerHTML='<option value="">Live session…</option>'+t.map(v=>{const T=Se(v.state),L=we(v.sessionId,v.eventClassName,v.capturedEvents,v.state);return`<option value="${j(v.sessionId)}" class="${T}" data-state="${j(v.state)}">${k(L)}</option>`}).join(""),c.innerHTML='<option value="">Saved report…</option>'+s.map(v=>`<option value="${j(v.fileName)}">${k(v.fileName)}</option>`).join("");const l=n||d,u=i||r;l&&t.some(v=>v.sessionId===l)&&(o.value=l);const m=a??((y=t.find(v=>v.sessionId===o.value))==null?void 0:y.state)??null;o.classList.remove("session-select-active","session-select-warn","session-select-stopped");const f=ne(m);f&&o.classList.add(f),u&&s.some(v=>v.fileName===u)?c.value=u:l&&(c.value="")}function C(e,t){var T;const s=Le(t),n=t.sessionState,i=Te(t),a=t.agentPresent?`agent v${t.protocolVersion}`:"agent absent",o=(T=t.serverStatus)==null?void 0:T.tps,c=o!=null?o.toFixed(1):"—",d=e.querySelector(".status-left"),r=d==null?void 0:d.querySelector(".trace-status-group");if(d&&t.liveMode){const L=ie(n,t.streamConnected);r&&(r.outerHTML=L)}const l=e.querySelector("#session-select");if(l){l.classList.remove("session-select-active","session-select-warn","session-select-stopped");const L=ne(n);L&&l.classList.add(L)}const u=e.querySelector(".status-left .status-highlight");u&&(u.textContent=s);const m=e.querySelectorAll(".status-left .status-highlight");m.length>=2&&(m[1].textContent=i);const f=e.querySelector("#status-tps");f&&(f.textContent=c,f.classList.toggle("tps-ok",o!=null&&o>=18),f.classList.toggle("status-highlight",o==null||o<18));const y=e.querySelector(".agent-pill");y&&(y.textContent=a,y.classList.toggle("absent",!t.agentPresent));const v=e.querySelector(".sidebar-footer");v&&(v.innerHTML=`<span>${k(t.paperVersion)}</span><br><span>${k(a)}</span>`)}function Le(e){var t,s;return((t=e.report)==null?void 0:t.session.sessionId)??e.selectedSessionId??((s=e.serverStatus)==null?void 0:s.activeTraceSessionId)??"—"}function Te(e){var n,i;const t=(i=(n=e.report)==null?void 0:n.dispatches.find(a=>a.worldName))==null?void 0:i.worldName;if(t)return t;const s=e.serverStatus;return s!=null&&s.defaultWorldName?`${s.defaultWorldName} · ${s.defaultGameMode} · ${s.onlinePlayers} players`:"—"}function k(e){return e.replaceAll("&","&amp;").replaceAll("<","&lt;").replaceAll(">","&gt;").replaceAll('"',"&quot;")}function j(e){return k(e).replaceAll("'","&#39;")}function G(e){if(e.listenerTimings.length>0)return e.listenerTimings;const t=e.listenerChain??[];if(t.length===0)return[];const s=Math.max(e.durationNanos,t.length),n=Math.floor(s/t.length);return t.map((i,a)=>{const o=a===t.length-1?s-n*(t.length-1):n;return{invocationOrder:i.registrationOrder??a+1,pluginName:i.pluginName,listenerClassName:i.listenerClassName,methodName:i.methodName,priority:i.priority,durationNanos:o,durationMillis:$(o),exceedsSlowThreshold:!1,threwException:!1,exceptionType:null}})}const Z=5e7;function Ke(e){var d;let t=0,s=0;const n=new Map;for(const r of e.dispatches)for(const l of G(r)){t+=1,l.exceedsSlowThreshold&&(s+=1);const u=`${l.pluginName}::${E(r.eventClassName)}`,m=n.get(u),f=l.durationNanos/Z*100,y=Qe(l,f);(!m||l.durationNanos>m.timeNanos)&&n.set(u,{plugin:l.pluginName,event:E(r.eventClassName),timeNanos:l.durationNanos,tickPercent:f,status:y})}const i=[...n.values()].sort((r,l)=>l.timeNanos-r.timeNanos).slice(0,8),a=[...e.dispatches].sort((r,l)=>l.sequence-r.sequence).slice(0,8).map(r=>et(r)),o=e.session.capturedEvents,c=((d=e.dispatches.find(r=>r.worldName))==null?void 0:d.worldName)??null;return{uptimeMillis:Me(e.session),eventsTraced:o,listenersInvoked:t,avgListenersPerEvent:o>0?t/o:0,flaggedListeners:s,topOffenders:i,recentTraces:a,primaryWorld:c}}function Qe(e,t){return e.exceedsSlowThreshold||t>=10?"critical":t>=3?"warn":"ok"}function et(e){const t=[...e.listenerTimings].sort((o,c)=>c.durationNanos-o.durationNanos)[0],s=e.durationNanos/Z*100;let n="ok";s>=15||t!=null&&t.exceedsSlowThreshold?n="critical":s>=5&&(n="warn");const i=E(e.eventClassName),a=e.blockMaterial??e.playerName??null;return{sequence:e.sequence,label:i,detail:a,startedAtMillis:e.startedAtMillis,durationNanos:e.durationNanos,severity:n}}function tt(e){return e==="critical"?"critical":e==="warn"?"warn":"ok"}function ae(e){return`${(e/Z*100).toFixed(1)}%`}function Me(e,t=Date.now()){return e.state==="ACTIVE"||e.state==="THROTTLED"?Math.max(0,t-e.startedAtMillis):e.durationMillis}function st(e){return e==="critical"?"offender-tick-crit":e==="warn"?"offender-tick-warn":"offender-tick-ok"}function nt(e){return e==="critical"?"severity-crit":e==="warn"?"severity-warn":"severity-ok"}function it(e,t){const s=Ke(t),n=s.topOffenders.length>0?s.topOffenders.map(a=>`
          <div class="offender-row">
            <div class="offender-plugin">${h(a.plugin)}</div>
            <div class="offender-event">${h(a.event)}</div>
            <div class="offender-time">${$(a.timeNanos)}</div>
            <div class="${st(a.status)}">${ae(a.timeNanos)}</div>
            <div><span class="pill pill-${a.status}">${tt(a.status)}</span></div>
          </div>`).join(""):'<div class="empty-cell">No listener timing data yet. Attach the EventLens agent for per-listener metrics.</div>',i=s.recentTraces.length>0?s.recentTraces.map(a=>`
          <div class="feed-row">
            <div class="feed-time">${Ae(a.startedAtMillis)}</div>
            <div class="feed-body">
              ${h(a.label)}${a.detail?` <span class="feed-detail">${h(a.detail)}</span>`:""}
            </div>
            <div class="feed-ms ${nt(a.severity)}">${$(a.durationNanos)}</div>
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
          <div class="stat-value" id="stat-session-uptime">${se(Me(t.session))}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">Events traced</div>
          <div class="stat-value">${s.eventsTraced.toLocaleString()}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">Listeners invoked</div>
          <div class="stat-value">${s.listenersInvoked.toLocaleString()}</div>
          <div class="stat-hint">avg ${s.avgListenersPerEvent.toFixed(1)} / event</div>
        </div>
        <div class="stat-card stat-card-alert">
          <div class="stat-label">Flagged listeners</div>
          <div class="stat-value stat-value-danger">${s.flaggedListeners}</div>
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
          ${n}
        </div>
        <div class="panel">
          <div class="panel-head">Recent trace feed</div>
          <div class="feed-list">${i}</div>
        </div>
      </div>
    </section>
  `}function ot(e,t){const s=e.querySelector("#stat-session-uptime");s&&(s.textContent=se(t))}const at=["LOWEST","LOW","NORMAL","HIGH","HIGHEST","MONITOR"];let A=0;function re(){A=0}function ee(e,t){var l,u;const s=R(t.session.state);if(!t.dispatches.length){e.innerHTML=`<section class="page"><header class="page-header"><h1>Timeline</h1></header>${s}<p class="empty">No dispatches captured.</p></section>`;return}A>=t.dispatches.length&&(A=0);const n=rt(t.dispatches,A),i=t.dispatches.indexOf(n),a=Math.max(12,Math.ceil(n.durationNanos/1e6)+1),o=n.durationNanos/Z*100,c=o>=15?"crit":o>=5?"warn":"ok",d=lt(n),r=dt(n,a);e.innerHTML=`
    <section class="page">
      <header class="page-header">
        <h1>Timeline</h1>
        <p class="page-subtitle mono">
          ${h(E(n.eventClassName))}${d?` · ${h(d)}`:""} · tick #${n.sequence} · total <span class="severity-${c}">${$(n.durationNanos)} (${o.toFixed(1)}% of tick budget)</span>
        </p>
      </header>
      ${s}
      <div class="timeline-panel">
        <div class="timeline-controls">
          <button type="button" class="btn-ghost" id="timeline-prev"${i<=0?" disabled":""}>← Previous</button>
          <span class="mono status-muted">Dispatch ${i+1} of ${t.dispatches.length}</span>
          <button type="button" class="btn-ghost" id="timeline-next"${i>=t.dispatches.length-1?" disabled":""}>Next →</button>
        </div>
        <div class="timeline-axis">
          ${ct(a).map(m=>`<span>${m}ms</span>`).join("")}
        </div>
        <div class="timeline-lanes">${r}</div>
      </div>
    </section>
  `,(l=e.querySelector("#timeline-prev"))==null||l.addEventListener("click",()=>{A=Math.max(0,i-1),ee(e,t)}),(u=e.querySelector("#timeline-next"))==null||u.addEventListener("click",()=>{A=Math.min(t.dispatches.length-1,i+1),ee(e,t)})}function rt(e,t){return t>=0&&t<e.length?e[t]:[...e].sort((s,n)=>n.durationNanos-s.durationNanos)[0]}function lt(e){const t=[];return e.blockMaterial&&t.push(e.blockMaterial),e.blockX!=null&&e.blockY!=null&&e.blockZ!=null&&t.push(`@ ${e.blockX},${e.blockY},${e.blockZ}`),t.join(" ")}function ct(e){const t=e<=12?2:Math.ceil(e/6),s=[];for(let n=0;n<=e;n+=t)s.push(n);return s[s.length-1]!==e&&s.push(e),s}function dt(e,t){const s=G(e),n=new Map;for(const o of s){const c=ft(o.priority),d=n.get(c)??[];d.push(o),n.set(c,d)}let i=0;const a=[];for(const o of at){const c=n.get(o);if(!(c!=null&&c.length))continue;c.sort((u,m)=>u.invocationOrder-m.invocationOrder);const d=o==="MONITOR",r=c.map(u=>{const m=i,f=u.durationNanos/1e6;i+=f;const y=m/t*100,v=Math.max(d?.6:.8,f/t*100),T=pt(u,f),L=d?"monitor":T,Ce=vt(u.pluginName),Oe=u.exceedsSlowThreshold?" · flagged":"",Ee=d?"":`<span>${h(Ce)} ${$(u.durationNanos)}${Oe}</span>`;return`
          <div class="timeline-bar bar-${L}" style="left:${y.toFixed(2)}%;width:${v.toFixed(2)}%;" title="${h(u.pluginName)}.${h(u.methodName)} · ${$(u.durationNanos)}">
            ${Ee}
          </div>`}).join(""),l=d&&c.length?`<div class="lane-note">${c.map(u=>`${h(u.pluginName)} ${$(u.durationNanos)}`).join(", ")} (read-only observers)</div>`:"";a.push(`
      <div class="timeline-lane-block">
        <div class="lane-label">${ut(o)}</div>
        <div class="lane-track">${r}</div>
        ${l}
      </div>`)}return a.length?a.join(""):`<div class="empty">${e.listenerTimings.length>0?"No listeners matched the timeline filters.":"No per-listener timings for this dispatch. Attach the EventLens agent for measured timings, or wait for the full trace sync."}</div>`}function ut(e){return e==="LOWEST"?"Lowest":e==="LOW"?"Low":e==="NORMAL"?"Normal":e==="HIGH"||e==="HIGHEST"?"High":"Monitor"}function pt(e,t){return e.exceedsSlowThreshold||t>=5?"critical":t>=2?"warn":"ok"}function ft(e){const t=e.toUpperCase();return t.includes("MONITOR")?"MONITOR":t.includes("LOWEST")?"LOWEST":t.includes("LOW")?"LOW":t.includes("HIGHEST")?"HIGHEST":t.includes("HIGH")?"HIGH":"NORMAL"}function vt(e){return e.length<=14?e:e.slice(0,12)+"…"}function ht(e,t){const s=R(t.session.state),n=mt(t.dispatches);if(!n){e.innerHTML=`<section class="page"><header class="page-header"><h1>Flame graph</h1></header>${s}<p class="empty">No dispatches captured.</p></section>`;return}if(!G(n).length){e.innerHTML=`<section class="page"><header class="page-header"><h1>Flame graph</h1></header>${s}<p class="empty">No listener timing data. Attach the EventLens agent for flame view, or wait for the full trace sync.</p></section>`;return}const i=E(n.eventClassName),a=G(n).sort((l,u)=>l.invocationOrder-u.invocationOrder),o=[...a].sort((l,u)=>u.durationNanos-l.durationNanos)[0],c=gt(a,n.durationNanos),d=yt(a,n.durationNanos,o),r=$t(o);e.innerHTML=`
    <section class="page">
      <header class="page-header">
        <h1>Flame graph</h1>
        <p class="page-subtitle mono">Call stack for the same trace · widths scaled to ${$(n.durationNanos)}</p>
      </header>
      ${s}
      <div class="flame-panel">
        <div class="flame-stack">
          <div class="flame-row">
            <div class="flame-block block-root">
              <span>${h(i)} · ${$(n.durationNanos)} · 100%</span>
            </div>
          </div>
          <div class="flame-row">${c}</div>
          ${d?`<div class="flame-row">${d}</div>`:""}
        </div>
        ${r?`<div class="flame-insight">${r}</div>`:""}
      </div>
    </section>
  `}function mt(e){return e.length?[...e].sort((t,s)=>s.durationNanos-t.durationNanos)[0]:null}function gt(e,t){let s=0;return e.map(n=>{const i=Math.max(.2,n.durationNanos/t*100),a=s;s+=i;const o=n.priority.toUpperCase().includes("MONITOR"),c=Nt(n,n.durationNanos/t),d=o?"monitor":c,r=o||i<3?"":`<span>${h(n.pluginName)}${i>=8?` · ${$(n.durationNanos)}`:""}</span>`;return`
        <div class="flame-block block-${d}" style="left:${a.toFixed(2)}%;width:${i.toFixed(2)}%;" title="${h(n.pluginName)}.${h(n.methodName)}">
          ${r}
        </div>`}).join("")}function yt(e,t,s){if(!s||s.durationNanos/t<.2)return"";const i=e.slice(0,e.indexOf(s)).reduce((r,l)=>r+l.durationNanos,0)/t*100,a=s.durationNanos/t*100,o=bt(s);if(o.length>1){let r=i;return o.map(l=>{const u=l.nanos/t*100,m=`
          <div class="flame-block block-sub block-sub-crit" style="left:${r.toFixed(2)}%;width:${Math.max(u,2).toFixed(2)}%;">
            <span>${h(l.label)} ${$(l.nanos)}</span>
          </div>`;return r+=u,m}).join("")}const c=e.filter(r=>r!==s&&r.durationNanos>=t*.03);if(!c.length)return`
      <div class="flame-block block-sub block-sub-crit" style="left:${i.toFixed(2)}%;width:${Math.max(a*.4,8).toFixed(2)}%;">
        <span>${h(s.methodName)} · ${$(s.durationNanos)}</span>
      </div>`;let d=i;return c.slice(0,4).map(r=>{const l=r.durationNanos/t*100,m=`
        <div class="flame-block block-sub block-sub-${r.exceedsSlowThreshold?"crit":"ok"}" style="left:${d.toFixed(2)}%;width:${Math.max(l,2).toFixed(2)}%;">
          <span>${h(r.methodName)} · ${$(r.durationNanos)}</span>
        </div>`;return d+=l,m}).join("")}function bt(e){const t=e.methodName.split(/(?=[A-Z])|_/).filter(Boolean);if(t.length<=1)return[];const s=Math.ceil(e.durationNanos/t.length);return t.map((n,i)=>({label:n,nanos:i===t.length-1?e.durationNanos-s*(t.length-1):s}))}function Nt(e,t){return e.exceedsSlowThreshold||t>.4?"critical":t>.15?"warn":"ok"}function $t(e){if(!e)return"";const t=ae(e.durationNanos);return`<div><span class="${e.exceedsSlowThreshold?"severity-crit":"severity-warn"}">${h(e.pluginName)}'s ${$(e.durationNanos)}</span> (${t} tick) is the dominant cost in this dispatch.</div>`}function St(e,t,s){const n=s?E(s.session.eventClassName):null,i=wt(t,n);if(!i.nodes.length){e.innerHTML='<section class="page"><p class="empty">No event graph data available.</p></section>';return}const a=960,o=420,c=i.edges.map(l=>{const u=i.nodes.find(L=>L.id===l.from),m=i.nodes.find(L=>L.id===l.to);if(!u||!m)return"";const f=u.x+70,y=u.y+30,v=m.x+70,T=m.y+30;return`<line class="flow-edge${l.primary?" primary":""}" x1="${f}" y1="${y}" x2="${v}" y2="${T}" />`}).join(""),d=i.nodes.map(l=>`
      <div class="flow-node${l.primary?" primary":""}" style="left:${l.x}px;top:${l.y}px">
        <div class="flow-node-title">${h(l.label)}</div>
        <div class="flow-node-sub">${h(l.sublabel)}</div>
      </div>`).join(""),r=n?`Cascades observed downstream of ${n} this session. Node size = frequency.`:"Event and plugin relationships across active trace sessions.";e.innerHTML=`
    <section class="page">
      <header class="page-header">
        <h1>Event graph</h1>
        <p class="page-subtitle">${r}</p>
      </header>
      ${s?R(s.session.state):""}
      <div class="flow-panel">
        <div class="flow-canvas" style="width:${a}px;height:${o}px">
          <svg class="flow-edges" width="${a}" height="${o}">${c}</svg>
          ${d}
        </div>
      </div>
    </section>
  `}function wt(e,t){const s=e.nodes.filter(r=>r.kind==="EVENT"),n=e.nodes.filter(r=>r.kind==="PLUGIN");if(!s.length&&!n.length)return{nodes:[],edges:[]};const i=s.find(r=>t&&r.label===t)??s.sort((r,l)=>l.weight-r.weight)[0]??null,a=s.filter(r=>r!==i).sort((r,l)=>l.weight-r.weight).slice(0,3),o=s.filter(r=>r!==i&&!a.includes(r)).sort((r,l)=>l.weight-r.weight).slice(0,1),c=[],d=[];if(i&&c.push({id:i.id,label:i.label,sublabel:`${i.weight.toLocaleString()} / session`,primary:!0,x:20,y:190}),a.forEach((r,l)=>{const u=[60,190,320];c.push({id:r.id,label:r.label,sublabel:`${r.weight.toLocaleString()} observed`,primary:l===0&&o.length>0,x:270,y:u[l]??190}),i&&d.push({from:i.id,to:r.id,primary:l===0})}),o.length&&a[0]){c.push({id:o[0].id,label:o[0].label,sublabel:`${o[0].weight.toLocaleString()} observed`,primary:!0,x:530,y:60}),d.push({from:a[0].id,to:o[0].id,primary:!0});const r=s.find(l=>!c.some(u=>u.id===l.id));r&&(c.push({id:r.id,label:r.label,sublabel:`${r.weight.toLocaleString()} observed`,primary:!1,x:790,y:60}),d.push({from:o[0].id,to:r.id,primary:!0}))}return a.length||n.sort((r,l)=>l.weight-r.weight).slice(0,4).forEach((r,l)=>{const u=[60,190,320,190];c.push({id:r.id,label:r.label,sublabel:`${r.weight.toLocaleString()} invocations`,primary:l===0,x:270+Math.floor(l/2)*260,y:u[l]??190}),i&&d.push({from:i.id,to:r.id,primary:l===0})}),{nodes:c,edges:d}}const te=["LOWEST","LOW","NORMAL","HIGH","MONITOR"];function Lt(e,t){if(!(t!=null&&t.dispatches.length)){const o=t?R(t.session.state):"";e.innerHTML=`${o?`<section class="page"><header class="page-header"><h1>Plugin graph</h1></header>${o}<p class="empty">No dispatch data for plugin graph.</p></section>`:'<section class="page"><p class="empty">No dispatch data for plugin graph.</p></section>'}`;return}const s=[...t.dispatches].sort((o,c)=>c.durationNanos-o.durationNanos)[0],n=Mt(s),a=te.map(o=>{const c=n.filter(d=>d.priority===o||o==="HIGH"&&d.priority==="HIGHEST");return`
      <div class="priority-column">
        <div class="priority-label">${Tt(o)}</div>
        ${c.length?c.map(d=>`
              <div class="plugin-card card-${d.status}">
                <div class="plugin-name">${h(d.plugin)}</div>
                <div class="plugin-time">${$(d.durationNanos)}</div>
                <div class="plugin-tick">${ae(d.durationNanos)} tick${d.status==="critical"?" · flagged":""}${d.readOnly&&d.status!=="critical"?" · read-only":""}</div>
              </div>`).join(""):""}
      </div>`}).map((o,c)=>c===0?o:`<div class="priority-arrow">›</div>${o}`).join("");e.innerHTML=`
    <section class="page">
      <header class="page-header">
        <h1>Plugin graph</h1>
        <p class="page-subtitle">Execution order for this trace, grouped by listener priority. Node color = severity.</p>
      </header>
      ${R(t.session.state)}
      <div class="plugin-panel">
        <div class="priority-grid">${a}</div>
      </div>
    </section>
  `}function Tt(e){return e==="LOWEST"?"Lowest":e==="LOW"?"Low":e==="NORMAL"?"Normal":e==="HIGH"?"High":"Monitor"}function Mt(e){const t=[];for(const s of G(e)){const n=kt(s.priority),i=s.durationNanos/5e7*100,a=n==="MONITOR",o=a?"monitor":s.exceedsSlowThreshold||i>=10?"critical":i>=3?"warn":"ok",c=a||s.pluginName.toLowerCase().includes("eventlens");t.push({plugin:s.pluginName,priority:n,durationNanos:s.durationNanos,status:o,readOnly:c})}return t.sort((s,n)=>{const i=te.indexOf(s.priority==="HIGHEST"?"HIGH":s.priority)-te.indexOf(n.priority==="HIGHEST"?"HIGH":n.priority);return i!==0?i:n.durationNanos-s.durationNanos})}function kt(e){const t=e.toUpperCase();return t.includes("MONITOR")?"MONITOR":t.includes("LOWEST")?"LOWEST":t.includes("LOW")?"LOW":t.includes("HIGHEST")?"HIGHEST":t.includes("HIGH")?"HIGH":"NORMAL"}const g=document.querySelector("#app");let p=null,M="overview",b="",le="",w="offline",F=!1,W=!1,B=null,ce=!1,de=0,ue="Paper —",N=null,ye=!1,H=null,U=null,Y=null,pe=[];function S(){return b||(p==null?void 0:p.session.sessionId)||""}function x(e){var t;(t=e.environment)!=null&&t.paperVersion&&(ue=e.environment.paperVersion),e.instrumentation&&(ce=e.instrumentation.agentPresent,de=e.instrumentation.protocolVersion)}function It(e){N=e,ce=e.agentPresent,de=e.protocolVersion,e.paperVersion&&(ue=e.paperVersion),!b&&!F&&e.activeTraceSessionId&&w!=="report"&&(b=e.activeTraceSessionId)}function z(){var t;const e=S();return e?((p==null?void 0:p.session.sessionId)===e?p.session.state:null)??((t=pe.find(s=>s.sessionId===e))==null?void 0:t.state)??null:(p==null?void 0:p.session.state)??null}function I(){return{activeView:M,liveMode:P(),streamConnected:W,agentPresent:ce,protocolVersion:de,paperVersion:ue,serverStatus:N,report:p,sessionState:z(),selectedSessionId:b,onNavigate:e=>{M=e,fe(e),O()},onSessionChange:e=>{ve(e,!0)},onReportChange:e=>{ke(e)},onFileLoad:e=>{At(e)}}}function J(){ye||(Ze(g,I()),ye=!0)}function fe(e){g.querySelectorAll(".nav-item[data-view]").forEach(t=>{t.classList.toggle("active",t.dataset.view===e)})}function Q(e){const t=De(e);if(!t)return!1;const s=S();return s&&s!==t.sessionId?!1:(b||(b=t.sessionId,w="session",_(g,t.sessionId,"")),p=We(p,t),C(g,I()),oe(g,t.sessionId,t.capturedEvents,t.eventClassName,t.state),O(!1),Ct(),!0)}function Ct(){H!==null&&window.clearTimeout(H),H=window.setTimeout(()=>{H=null,xt()},400)}async function Ot(){if(!(w!=="session"||!S()))try{const e=await V(S());if(S()!==e.session.sessionId)return;p=e,x(e),C(g,I()),oe(g,e.session.sessionId,e.session.capturedEvents,e.session.eventClassName,e.session.state)}catch{}}function Et(){return p!=null&&p.dispatches.length?p.dispatches.some(e=>{var t;return e.listenerTimings.length===0&&(((t=e.listenerChain)==null?void 0:t.length)??0)===0}):!0}async function xt(){if(!(w!=="session"||!S()))try{const e=await V(S());if(S()!==e.session.sessionId)return;p=e,x(e),C(g,I()),oe(g,e.session.sessionId,e.session.capturedEvents,e.session.eventClassName,e.session.state),O(!1)}catch{}}async function X(){const[e,t]=await Promise.all([Ne(),$e()]);return pe=e,Je(g,e,t,b,le,z()),e}async function ve(e,t){b=e,le="",w="session",F=t,re(),_(g,e,""),p=await V(e),x(p),C(g,I()),await O(!1)}async function ke(e){le=e,b="",w="report",F=!1,re(),_(g,"",e),p=await Re(e),x(p),C(g,I()),await O(!1)}async function At(e){w="offline",F=!1,re(),p=Fe(await e.text()),x(p),M="overview",J(),fe(M),await O(!1)}async function he(e){const t=e.filter(n=>n.state==="ACTIVE");if(!t.length||b!==""&&t.some(n=>n.sessionId===b))return!1;if(!b||!F||w!=="session"){const n=t[0];return await ve(n.sessionId,!1),!0}return!1}async function Ie(){w!=="session"||!S()||(p=await V(S()),x(p),C(g,I()),await O(!1))}async function me(){if(P())try{const e=await Pe();It(e),C(g,I())}catch{}}function Ht(){const e=z();if(p&&e&&ge(e))return p.session.startedAtMillis;if(N!=null&&N.activeTraceSessionId&&N.activeTraceStartedAtMillis>0){const s=N.activeTraceSessionId;if(!S()||S()===s)return N.activeTraceStartedAtMillis}const t=pe.find(s=>s.sessionId===S());return t&&ge(t.state)?t.startedAtMillis:null}function Pt(){const e=Ht();if(e==null)return;const t=Math.max(0,Date.now()-e),s=g.querySelector("#view-root");s&&M==="overview"&&ot(s,t)}function Rt(){Y===null&&(Y=window.setInterval(Pt,1e3))}function qt(){U===null&&(U=window.setInterval(()=>{me()},2e3))}async function be(){if(!P())return;await me();const e=await X();await he(e)||w==="session"&&b&&await Ie()}function Ft(e,t){if(e==="session-started"){if(Q(t))return;(async()=>{await X();const s=typeof t.sessionId=="string"?t.sessionId:"";s&&(!S()||!F)?await ve(s,!1):await he(await Ne())})();return}if(e==="dispatch"){if(Q(t))return;Ie();return}if(e==="poll"){be();return}if(e==="session-stopped"){if(Q(t)){X();return}be()}}async function Dt(){if(J(),!P())return;await me(),C(g,I()),qt(),Rt();const e=await X(),t=await $e();if(e.length){if(await he(e),!b&&(N!=null&&N.activeTraceSessionId)&&(b=N.activeTraceSessionId,w="session"),b&&!p)try{p=await V(b),x(p),_(g,b,"")}catch{p=null}}else t.length&&t[0].format==="json"&&await ke(t[0].fileName);const s=Xe((i,a)=>{Ft(i,a)});B=s.stop;const n=()=>{const i=s.connected();i!==W&&(W=i,ze(g,W,z()))};n(),window.setInterval(n,1e3)}function O(e=!0){J();const t=g.querySelector("#view-root");return t?(fe(M),e&&P()&&w==="session"&&!p?(t.innerHTML='<p class="empty">Refreshing live trace data…</p>',Promise.resolve()):(P()&&w==="session"&&Et()?Ot():Promise.resolve()).then(()=>{if(M==="events"){qe().then(n=>St(t,n,p)).catch(()=>{t.innerHTML='<section class="page"><p class="empty">Failed to load event graph.</p></section>'});return}if(M==="plugins"){Lt(t,p);return}if(!p){if(N!=null&&N.activeTraceSessionId){const n=N.activeTraceStartedAtMillis>0?Math.max(0,Date.now()-N.activeTraceStartedAtMillis):0;t.innerHTML=`
          <section class="page">
            <header class="page-header">
              <h1>Overview</h1>
              <p class="page-subtitle">Trace session is active. Waiting for first dispatch…</p>
            </header>
            <div class="stat-grid">
              <div class="stat-card">
                <div class="stat-label">Session uptime</div>
                <div class="stat-value" id="stat-session-uptime">${se(n)}</div>
              </div>
            </div>
            <p class="empty">Interact in-game to capture events, or wait for the live feed to populate.</p>
          </section>`}else t.innerHTML='<section class="page"><p class="empty">No active trace session. Start one with <span class="mono">/eventlens trace start &lt;Event&gt;</span>.</p></section>';return}M==="overview"?it(t,p):M==="timeline"?ee(t,p):M==="flame"&&ht(t,p)})):Promise.resolve()}J();Dt().then(()=>O(!1)).catch(e=>{const t=g.querySelector("#view-root");t&&(t.innerHTML=`<p class="empty">Failed to load dashboard: ${e}</p>`)});window.addEventListener("beforeunload",()=>{B==null||B(),H!==null&&window.clearTimeout(H),U!==null&&window.clearInterval(U),Y!==null&&window.clearInterval(Y)});
