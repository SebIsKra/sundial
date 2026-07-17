SETTING UP W DOCKER:

1. Make sure you are using JAVA 21
2. Install Docker on your desktop

3. Run this in Terminal:
        git clone <repo-url>
        cd sundial/demo
        docker compose up -d
        chmod +x mvnw        // Mac/Linux only(needed if permission is denied)
        ./mvnw spring-boot:run

Application should now be running
URL: http://localhost:8080

ESTABLISHING JDBC Connection: --> NOT YET DONE!!
(This is to make sure the Java application can communicate with the Database)

https://www.geeksforgeeks.org/java/establishing-jdbc-connection-in-java/

-----------------------------------------
Further Information about the setup: 

Der eine Prozess: deine Java-App
Wenn du ./mvnw spring-boot:run ausführst, startet dein Terminal einen JVM-Prozess (Java Virtual Machine) — das ist das Programm, das deinen kompilierten Java-Code tatsächlich ausführt. Darin läuft dein Spring-Boot-Code, inklusive dem eingebetteten Tomcat-Webserver und dem HikariCP-Pool. Dieser Prozess ist an dein Terminal-Fenster "angehängt" — er läuft im Vordergrund, blockiert die Eingabezeile, und gibt dir laufend Logs aus.

Der andere Prozess: Docker/Postgres
Ganz unabhängig davon läuft im Hintergrund die Docker-Engine (gestartet durch Docker Desktop), und darin dein Postgres-Container sundial-db. Der wurde separat mit docker compose up -d gestartet — das -d steht für "detached", also losgelöst vom Terminal. Deshalb läuft er weiter, egal was in deinem Java-Terminal passiert.

Warum Ctrl+C die App stoppt, aber nicht Postgres
Ctrl+C schickt ein Stopp-Signal an genau den Prozess, der gerade im Vordergrund deines Terminals läuft — also deine JVM/Spring-Boot-App. Der Docker-Container ist ein komplett separater Prozess, verwaltet von der Docker-Engine, nicht von deinem Terminal. Deshalb bleibt er unberührt und läuft munter weiter.

Wie die beiden trotzdem zusammenarbeiten
Deine App spricht mit Postgres nicht, weil sie "im selben Programm" laufen, sondern weil sie über das Netzwerk kommunizieren — genau wie eine App mit einer Webseite spricht. localhost:5432 ist die Adresse, unter der der Postgres-Container erreichbar ist, und HikariCP baut darüber die Verbindung auf.    