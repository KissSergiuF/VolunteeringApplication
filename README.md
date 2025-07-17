# 🌍 Platformă web pentru voluntariat

Această aplicație web facilitează identificarea, organizarea și participarea la activități de voluntariat printr-o hartă interactivă. Utilizatorii pot vizualiza evenimente, comunica în timp real prin chat, oferi și primi feedback, iar organizațiile pot gestiona eficient evenimentele și voluntarii implicați.

---

## ⚙️ Instalare și rulare (testat pe Windows)

Pentru rularea aplicației este necesară instalarea următoarelor componente software, testate pe un sistem Windows cu instalare proaspătă. Se recomandă utilizarea versiunilor indicate, pentru compatibilitate maximă.

### Instalare Java JDK 17

- Se descarcă JDK 17 de la: https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html
- Se verifică instalarea cu:
  bash
  java -version
  

### Instalare Maven

- Se descarcă versiunea 3.9.10 de la: https://maven.apache.org/download.cgi
- Verificare:
  bash
  mvn -v
  

### Instalare Node.js LTS

- Se descarcă de la: https://nodejs.org/en
- Verificare:
  bash
  node -v
  npm -v
  

### Instalare Angular CLI

- Se instalează cu:
  bash
  npm install -g @angular/cli
  
- Verificare:
  bash
  ng version
  

*Notă:* Angular CLI versiunea 20.x este compatibilă cu proiectul (Angular 19).

### Instalare PostgreSQL

- Se descarcă versiunea 17.5 de la: https://www.enterprisedb.com/downloads/postgres-postgresql-downloads
- După instalare, se creează o bază de date în PgAdmin

În PgAdmin, click dreapta pe baza de date → Query Tool → se rulează codul din Backend/src/main/resources/schema.sql 

### Clonare proiect și rulare aplicație backend

bash
git clone <URL_PROIECT>
cd Backend
mvn install


Configurează application.yml cu datele bazei de date și email-ul pentru trimiterea notificărilor.

Rulare backend:
bash
mvn spring-boot:run


### Rulare aplicație frontend

bash
cd Frontend
npm install
ng serve --open


### Configurare Google Maps API

1. Accesează: [https://developers.google.com/maps](https://developers.google.com/maps)
2. Urmează pașii:
   - Click pe *Get Started*
   - Creează un nou proiect
   - Generează o cheie API
3. În fișierul index.html, înlocuiește your_api_key cu cheia generată.


---

## 🧱 Arhitectura

- *Frontend:* Angular 19
- *Backend:* Java 17, Spring Boot
- *Baza de date:* PostgreSQL

---

## 🚀 Direcții viitoare

- Autentificare cu conturi Google/Facebook
- Chat extins cu funcționalități avansate (reacții, atașamente)
- Sistem de notificări în timp real
- Algoritmi inteligenți de recomandare a evenimentelor
- Export statistici personalizate pentru organizatori
