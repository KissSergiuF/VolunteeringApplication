# ?? Platform? web pentru voluntariat

Aceast? aplica?ie web faciliteaz? identificarea, organizarea ?i participarea la activit??i de voluntariat printr-o hart? interactiv?. Utilizatorii pot vizualiza evenimente, comunica în timp real prin chat, oferi ?i primi feedback, iar organiza?iile pot gestiona eficient evenimentele ?i voluntarii implica?i.

---

## ?? Instalare ?i rulare (testat pe Windows)

Pentru rularea aplica?iei este necesar? instalarea urm?toarelor componente software, testate pe un sistem Windows cu instalare proasp?t?. Se recomand? utilizarea versiunilor indicate, pentru compatibilitate maxim?.

### Instalare Java JDK 17

- Se descarc? JDK 17 de la: https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html
- Se verific? instalarea cu:
  ```bash
  java -version
  ```

### Instalare Maven

- Se descarc? versiunea 3.9.10 de la: https://maven.apache.org/download.cgi
- Verificare:
  ```bash
  mvn -v
  ```

### Instalare Node.js LTS

- Se descarc? de la: https://nodejs.org/en
- Verificare:
  ```bash
  node -v
  npm -v
  ```

### Instalare Angular CLI

- Se instaleaz? cu:
  ```bash
  npm install -g @angular/cli
  ```
- Verificare:
  ```bash
  ng version
  ```

**Not?:** Angular CLI versiunea 20.x este compatibil? cu proiectul (Angular 19).

### Instalare PostgreSQL

- Se descarc? versiunea 17.5 de la: https://www.enterprisedb.com/downloads/postgres-postgresql-downloads
- Dup? instalare, se creeaz? o baz? de date în PgAdmin

În PgAdmin, click dreapta pe baza de date ? Query Tool ? se ruleaz? codul din `Backend/src/main/resources/schema.sql` 

### Clonare proiect ?i rulare aplica?ie backend

```bash
git clone <URL_PROIECT>
cd Backend
mvn install
```

Configureaz? `application.yml` cu datele bazei de date ?i email-ul pentru trimiterea notific?rilor.

Rulare backend:
```bash
mvn spring-boot:run
```

### Rulare aplica?ie frontend

```bash
cd Frontend
npm install
ng serve --open
```

### Configurare Google Maps API

1. Acceseaz?: [https://developers.google.com/maps](https://developers.google.com/maps)
2. Urmeaz? pa?ii:
   - Click pe **Get Started**
   - Creeaz? un nou proiect
   - Genereaz? o cheie API
3. În fi?ierul `index.html`, înlocuie?te `your_api_key` cu cheia generat?.


---

## ?? Arhitectura

- **Frontend:** Angular 19
- **Backend:** Java 17, Spring Boot
- **Baza de date:** PostgreSQL

---

## ?? Direc?ii viitoare

- Autentificare cu conturi Google/Facebook
- Chat extins cu func?ionalit??i avansate (reac?ii, ata?amente)
- Sistem de notific?ri în timp real
- Algoritmi inteligen?i de recomandare a evenimentelor
- Export statistici personalizate pentru organizatori