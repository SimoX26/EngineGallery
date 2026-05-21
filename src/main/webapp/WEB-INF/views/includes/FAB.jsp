<%@ page contentType="text/html; charset=UTF-8" language="java" %>

<style>
@media (min-width: 769px) {
  .fab-mobile-center {
    display: none !important;
  }
}

@media (max-width: 768px) {
  .fab-mobile-center {
    left: 50% !important;
    right: auto !important;
    transform: translateX(-50%) !important;
    bottom: 20px !important;
  }
}
</style>

<a href="<%= request.getContextPath() %>/upload"
   class="fab-mobile-center"
   title="Carica immagine"
   style="
     position: fixed !important;
     right: 24px !important;
     bottom: 24px !important;
     width: 64px !important;
     height: 64px !important;
     border-radius: 50% !important;
     display: flex !important;
     align-items: center !important;
     justify-content: center !important;
     background: #1f2933 !important;
     color: #fff !important;
     font-size: 28px !important;
     text-decoration: none !important;
     box-shadow: 0 20px 50px rgba(0,0,0,0.35) !important;
     z-index: 2147483647 !important;
     pointer-events: auto !important;
   ">
    +
</a>
