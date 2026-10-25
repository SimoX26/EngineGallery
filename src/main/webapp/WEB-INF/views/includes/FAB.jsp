<%@ page contentType="text/html; charset=UTF-8" language="java" %>

<style>
  .fab-mobile-center {
    display: none;
    position: fixed;
    left: 50%;
    bottom: max(20px, env(safe-area-inset-bottom));
    z-index: 1040;
    align-items: center;
    justify-content: center;
    gap: 0.55rem;
    max-width: calc(100vw - 32px);
    padding: 0.8rem 1.2rem;
    border-radius: 999px;
    background: #1f2933;
    color: #fff;
    box-shadow: 0 20px 50px rgba(0, 0, 0, 0.35);
    font-weight: 650;
    text-decoration: none;
    white-space: nowrap;
    transform: translateX(-50%);
  }

  .fab-mobile-center:hover,
  .fab-mobile-center:focus-visible {
    background: #374151;
    color: #fff;
    text-decoration: none;
  }

  .fab-mobile-center span {
    font-size: 1.3rem;
    line-height: 1;
  }

  @media (max-width: 768px) {
    .fab-mobile-center {
      display: inline-flex;
    }
  }
</style>

<a href="<%= request.getContextPath() %>/upload"
   class="fab-mobile-center"
   title="Aggiungi motore">
    <span aria-hidden="true">+</span> Aggiungi motore
</a>
