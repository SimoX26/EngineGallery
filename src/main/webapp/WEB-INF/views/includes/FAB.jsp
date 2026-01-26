<%@ page contentType="text/html; charset=UTF-8" language="java" %>

<a href="<%= request.getContextPath() %>/image-upload"
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

<script>
  // Hardening: se qualche wrapper crea un contesto “strano”, assicuriamo che stia come ultimo nodo del body.
  (function () {
    const el = document.currentScript.previousElementSibling;
    if (el && el.parentNode !== document.body) {
      document.body.appendChild(el);
    } else if (el) {
      document.body.appendChild(el); // lo mette comunque in fondo (sopra tutto)
    }
  })();
</script>
