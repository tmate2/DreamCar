# DreamCar

Egy Jakarta EE Servleteken alapuló egyszerű web alkalmazás, ahol a felhasználók összegyűjthetik a kedvenc autóikat.

## Követelmények
 - [Tomcat szerver](https://tomcat.apache.org/), amin fut majd a webalkalmazás
 - [MySQL szerver](https://dev.mysql.com/downloads/mysql/) az adatbázis eléréséhez
 - [Maven](https://maven.apache.org/) az alkalmazás fordításához és csomagolásához
 - [JDK 21](https://jdk.java.net/21/)

## Telepítés
(Ha a követelményekben felsoroltak már telepítve vannak.)
#### 1. Lépés: Adatbázis szerver
 - Hozzuk létre a MySQL adatbázist és a hozzátartozó táblákat a `adatbazis_minta.sql` alapján.

#### 2. Lépés: Konfigurálás
 - Az `src/main/WEB-INF/properties.conf` állományban állítsuk be a szükséges paramétereket az adatbázis szerverhez való csatlakozáshoz.

#### 3. Lépés: Fordítása és csomagolás
 - A projekt főkönyvtárában a `mvn clean` paranccsal eltávolítjuk az esetleges korábbi fordításból származó fájlokat.
 - A `mvn compile`-al újrafordítjuk a projektet a `target` mappába.
 - Ha sikeresen fordult a projekt, utána az `mvn package` parancs segítségével elkészítjük a `target\DreamCar-1.0.war` fájlt.

#### 4. Lépés: Tomcat szerver
 - Az elkészült `target\DreamCar-1.0.war` állományt bemásoljuk a Tomcat szerver `webapps` könyvtárjába.
 - Indítsuk el a szervert. Ha már fut, akkor automatikusan észleli és kicsomagolja magának a `war` fájlt és futtatja a web alkalmazásunkat. (Ha mégse akkor próbáljuk meg újraindítani.)
 - Ha minden sikerült akkor böngészőből a `http://<tomcat-ip>:<port>/DreamCar-1.0` URI-n érhetjük el az alkalmazásunkat.

## Egyéb
- Ha az adatbázisban nem szerepel aktív admin jogosultságú felhasználó, akkor az alkalmazás induláskor létrehoz egy alap Adminisztrátor nevű felhasználót.<br>Belépési adatai: felhasználónév: `admin`, jelszó: `admin`.
- Az admin jogú felhasználók is a jelszavukat a sima felhasználói felületről tudják megváltoztatni.

##### Általam ismert hiányosságok:
- Táblák közötti összeköttetés hiányzik (nem tudtam jól megtervezni, de a programon belül jól működik)
- Nincs lehetőség törölni a "kedvenc autókat" (nem volt időm megírni), ha már felvettük. Csak akkor törlődik, ha törlődik a felhasználó vagy a típus vagy a márka...
- Nincs automatikus zárolás 5 elrontott bejelentkezési kísérlet után.
- Nem működik a fájlfeltöltés, egy alapértelmezett kép kerül a kedvenc autókhoz.