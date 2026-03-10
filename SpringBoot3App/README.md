This project is about building a Spring Boot 3 API with Angular, using MySQL Workbench. I followed a YouTube video (all links are listed under References in the README.md) to learn more about the Spring Boot framework and Angular with MySQL. The website is designed for invoice management. I have completed it on my localhost but I have not created additional files such as Docker for this project. I also attached some screenshots of the website. Although some parts are missing, I think it is fine at my current level. I would like to thank the YouTuber for creating such an amazing project.


References :
https://www.youtube.com/watch?v=tX7t45m-4H8 

https://copilot.microsoft.com 

https://unsplash.com/s/photos/person

https://mockaroo.com/ 

https://www.bootdey.com/snippets/view/bs4-invoice 

npm i jspdf
https://www.npmjs.com/package/jspdf 

org.apache.poi 

npmjs.com/package/file-saver

https://angular-doc.ru/guide/architecture-modules 

什麼是延遲載入？
延遲載入是指根據需要載入網站的元件、模組或其他資產的過程。 延遲載入模組有助於我們縮短啟動時間。 使用延遲載入，我們的應用程式不需要一次載入所有內容，它只需要載入應用程式首次載入時使用者期望看到的內容。 延遲載入的模組只有在使用者導航到其路線時才會載入。
https://medium.com/@kevinmavani/an-introduction-to-angular-modules-db75c3ebf842

https://www.npmjs.com/package/angular-notifier
npm install angular-notifier@13 (for this project Auglur 15.x)
這樣 angular-notifier 就會被安裝到這個專案的 node_modules，並且寫入 package.json 的 dependencies。


- mvn spring-boot:run = 用 Maven 啟動 Spring Boot 應用程式。
  好處：快速、方便，不需打包。
  適合開發測試；正式部署時通常會用 java -jar 或容器化方式。 

- CONTAINER_PORT=4000 mvn spring-boot:run 
  在啟動 Spring Boot 應用程式時，把環境變數 CONTAINER_PORT 設定為 4000，讓伺服器在 4000 埠上執行。

- remove console.log(response) before deploying an application 


- ✅ 總結 :
      容器：像是「應用程式的盒子」，裡面裝好程式與依賴。
      AWS ECS：像是「容器的管理員」，負責安排、監控、擴展這些盒子。
      兩者的關係：容器是執行單位，ECS 是管理平台。
      👉 一句話：ECS 幫你在 AWS 上自動化地運行與管理容器，讓容器化應用程式能更容易部署與擴展。

- 為什麼要先複製 pom.xml 並下載依賴，再複製完整程式碼?
📖 背後的原因 :
在 Docker 建置過程中，每一個指令（COPY、RUN 等）都會形成一個 建置快取層（layer cache）。
如果某一層的內容沒有改變，Docker 就會直接使用快取，而不用重新執行。
🔹 如果直接複製整個專案 :
COPY . /app
RUN mvn dependency:resolve
每次程式碼有任何改動（哪怕只是修改一個 Java 檔），Docker 都會認為 COPY . /app 這一層改變了。
結果：mvn dependency:resolve 這一層也要重新執行 → 重新下載所有依賴 → 非常耗時。
🔹 改成先複製 pom.xml :
COPY pom.xml /app
RUN mvn dependency:resolve
COPY . /app
第一步：只複製 pom.xml，如果 pom.xml 沒改變，Docker 就能沿用快取。
第二步：下載依賴（這通常是最耗時的步驟）。
第三步：再複製完整程式碼。即使程式碼改了，只要 pom.xml 沒變，依賴就不用重新下載。

======================================================================

- 📖 指令流程解釋
1. mvn package -DskipTests -X

    用 Maven 打包專案，產生 JAR 檔。
    -DskipTests → 跳過測試，縮短打包時間。
    -X → 顯示除錯資訊（verbose mode），方便排錯。
    打包完成後，成果會放在 target/ 資料夾。

2. cd target/
    進入 Maven 打包輸出的目錄。
    這裡會有 JAR 檔和其他編譯成果。
3. ls
    列出 target/ 裡的檔案。
    你看到的 SpringBoot3-0.0.1-SNAPSHOT.jar 就是可執行 JAR。

4. java -jar SpringBoot3-0.0.1-SNAPSHOT.jar
      用 Java 執行打包好的 Spring Boot 應用程式。
      這會啟動內建的 Tomcat 伺服器，並載入你的應用程式。
      如果 application.yml 裡設定了 server.port，就會在指定的埠號啟動。
5. ✅ 總結 :
這一連串指令的作用是：
先用 Maven 打包專案 → 進入輸出目錄 → 找到 JAR 檔 → 用 Java 執行 JAR → 啟動 Spring Boot 應用程式。


可以在另一台電腦開啟並執行同一個 Angular 專案，只要按照以下步驟操作即可：

1. 複製或下載專案
如果你有使用 Git，直接在另一台電腦上 clone 專案：
git clone <你的專案網址>
如果沒有 Git，就把整個 SpringBoot3App 專案資料夾複製到另一台電腦。

2. 安裝 Node.js 與 npm
確保另一台電腦已安裝 Node.js（建議版本與原本電腦一致）。
檢查版本：
node -v
npm -v

3. 安裝專案依賴套件
進入專案目錄：
cd ~/Desktop/SpringBoot3/SpringBoot3App
執行：
npm install
這會依照 package.json 安裝所有需要的套件（例如你之前加的 angular-notifier）。

4. 啟動專案
在專案目錄下執行：
ng serve
然後在瀏覽器打開 http://localhost:4200 就能看到你的 Angular 專案。

5. ⚠️ 注意事項
Angular CLI：另一台電腦也需要安裝 Angular CLI：
npm install -g @angular/cli
環境設定：如果專案有連接後端（例如 Spring Boot API），要確認環境變數或 API URL 在新電腦上設定正確。
Node 版本一致性：盡量保持 Node.js 版本一致，避免依賴衝突。

6. 總結  
是的，你可以在另一台電腦開啟專案。只要把專案複製或 clone 下來，安裝 Node.js 與 Angular CLI，進入專案目錄執行 npm install，最後用 ng serve 就能跑起來。



Module Architecture :
-login, create an account, forget passwaord
-home page
-related to static(Total customers/invoices/Billed)
-related to a customer/ all customers
-related to an invoice/ invoices
-profile page 
-narbar module
-core module
-share module (things are shared thoughout the entire application)


清晰的架構分層
Angular 專案常見分層：
  1. AppModule：應用程式入口，匯入 CoreModule 與 SharedModule。
  2. CoreModule：放全域服務、攔截器、單例物件。
  3. SharedModule：放共用元件、指令、管道（可在多個 Feature Module 重複使用）。
  4. Feature Modules：各功能模組，專注於特定業務邏輯。
這樣的分層能讓專案結構更清晰，維護更容易。

👉 簡單來說：
2. CoreModule 是專案的「核心大腦」，負責全域性的東西；
3. SharedModule 是「工具箱」，負責共用的元件/管道；
4. Feature Module 則是「專業部門」，各自處理不同功能。

# what is the difference between 
"  "scripts": [
              "node_modules/bootstrap/dist/js/bootstrap.min.js"
              ]" 
and 
"   "scripts": [
              "node_modules/bootstrap/dist/js/bootstrap.bundle.min.js"
            ]"

這兩個設定的差異在於 Bootstrap 的 JavaScript檔案內容：
📌 bootstrap.min.js
這是 純 Bootstrap JavaScript，只包含 Bootstrap 元件本身的互動邏輯（像是 Modal、Dropdown、Tooltip 等）。
但是它 不包含 Popper.js。
Popper.js 是 Bootstrap 用來處理 Tooltip 和 Dropdown 的定位的外部依賴。
如果你只引入 bootstrap.min.js，那麼像 Dropdown、Tooltip 這些功能可能會出現定位錯誤或無法正常運作，除非你另外手動安裝並引入 Popper.js。
📌 bootstrap.bundle.min.js
這是 Bootstrap + Popper.js 的整合版本。
它除了包含 Bootstrap 的 JavaScript 邏輯，還內建了 Popper.js。
使用這個檔案，你就不需要額外引入 Popper.js，所有 Bootstrap 元件（包含 Tooltip、Dropdown）都能正常運作。
✅ 建議
在 Angular 專案裡，通常直接用 bootstrap.bundle.min.js 就好，因為它已經包含 Popper.js，避免你還要額外安裝或引入。


USE SpringDB;
SET SQL_SAFE_UPDATES = FALSE;
UPDATE customer SET status = 'ACTIVE' WHERE MOD(id, 2) =1;
UPDATE customer SET status = 'INACTIVE' WHERE MOD(id, 2) =0;
UPDATE customer SET type = 'INDIVIDUAL';

2. SET SQL_SAFE_UPDATES = FALSE;
關閉 MySQL 的「安全更新模式」。
在安全模式下，如果 UPDATE 或 DELETE 沒有使用 WHERE 條件或沒有主鍵限制，MySQL 會拒絕執行。
設成 FALSE → 允許執行這些更新語句。
3. UPDATE customer SET status = 'ACTIVE' WHERE MOD(id, 2) = 1;
更新 customer 表裡 id 為奇數 的紀錄，把 status 欄位設為 'ACTIVE'。
MOD(id, 2) → 計算 id 除以 2 的餘數。
奇數 → 餘數 = 1。
偶數 → 餘數 = 0。


你的語句裡確實有 WHERE MOD(id, 2) = 1 條件。這樣的語句在 一般情況下 是合法的，因為它有 WHERE 條件限制，並且會只更新符合條件的紀錄（id 為奇數的紀錄）。
但是 MySQL 的 安全更新模式 (SQL_SAFE_UPDATES) 還有另一個規則：
除了必須有 WHERE 條件之外，這個 WHERE 條件必須 使用索引欄位（例如主鍵或有索引的欄位）。
如果 WHERE 條件裡用的是非索引運算（像 MOD(id, 2)），MySQL 會認為這樣的更新「不安全」，因為它可能會掃描整張表。
所以即使你有 WHERE MOD(id, 2) = 1，在安全模式下 MySQL 還是會拒絕執行，因為這個條件不是直接用索引比對，而是計算後的結果。



關鍵重點 :
✅ 第一個參數永遠是 URL，這是必須的。
✅ 第二個參數依方法不同：
      GET / DELETE → 第二個參數是 options。
      POST / PUT / PATCH → 第二個參數是 body，✅ 第三個才是 options。
✅ 如果 API 不需要 body，但方法要求（像 PATCH），就傳空 {}。
✅ 如果 API 要驗證或帶額外資訊，就在 options 裡加 headers 或 params。


👉 macOS x64 DMG Installer (178.53 MB)
======================================================
======================================================
======================================================


1. no space in disk => 清理了 ~/Library/Caches/* 之后再次登录，出现 401 Unauthorized
=> ng serve => localhost ＝> can login account 有localstorage (access token & refresh token) ，user profile 有 data，但係invoice page and customer list page  冇 show data，但係MySQL啲data包括 User 和 Customer data 仲係到

2. retart the computer 

3. 重启电脑后发现 Spring Boot 应用无法运行，通常不是因为 Spring Boot 本身坏掉了，而是因为环境或启动方式发生了变化。

解决步骤
4. mvn spring-boot:run
你看到的提示：
zsh: command not found: mvn

解决方法
安装 Maven
5. brew install maven

验证安装
安装完成后，运行：
6. mvn -v
如果输出 Maven 版本信息，就说明安装成功。

7. mvn spring-boot:run
你现在遇到的错误信息：[ERROR] No plugin found for prefix 'spring-boot' ...
意思是：Maven 在你的项目里找不到 Spring Boot 插件，所以无法执行 mvn spring-boot:run。
为什么会这样 ? 
(1) 缺少 Spring Boot 插件配置
      => 在 pom.xml 里必须声明 Spring Boot 插件，否则 Maven 不认识 spring-boot:run 这个命令。
(2) 你不在项目目录里运行
      => 如果你在用户主目录（~）运行 mvn spring-boot:run，Maven 会找不到项目的 pom.xml，自然也就没有插件。

谢谢你贴出完整的 pom.xml，我帮你确认一下：
关键点
你的 pom.xml 已经正确继承了 Spring Boot Starter Parent：
xml
<parent>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-parent</artifactId>
  <version>3.2.2</version>
</parent>
在 <build> 部分也已经声明了 spring-boot-maven-plugin：
xml
<plugin>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-maven-plugin</artifactId>
</plugin>
这说明你的 pom.xml 配置是正确的，可以支持 mvn spring-boot:run。
为什么之前报错
你之前运行 mvn spring-boot:run 的时候是在 用户主目录 (~) 下执行的，而不是在项目目录里。
Maven 在 ~ 下找不到 pom.xml，所以报错说没有找到 spring-boot 插件。
正确的运行方式
进入项目目录  
假设你的项目在 ~/Desktop/SpringBoot3：

8. cd ~/Desktop/SpringBoot3
运行 Spring Boot 应用
bash
9. mvn spring-boot:run

✅ 总结：
你的 pom.xml 已经没问题，报错的原因是你在错误的目录运行了 Maven。只要进入项目目录再运行，就能启动 Spring Boot 应用。

10. 点击右侧 Maven → Reload。
点击 Maven → Reload 就是告诉 IntelliJ：
“请重新读取我的 pom.xml，下载依赖，更新项目结构和配置。”
这样 IDE 才能正确识别入口类的 main 方法，Run 按钮才会出现。

=============================================================================================================================================================================

这条 SQL 语句的作用是：
UPDATE customer 
SET created_at = CURRENT_TIMESTAMP 
WHERE id > 2;

解释 :
1. UPDATE customer：表示要更新 customer 这张表的数据。
2. SET created_at = CURRENT_TIMESTAMP：把 created_at 字段的值更新为当前时间（数据库执行语句时的系统时间）。
3. WHERE id > 2：只更新 id 大于 2 的那些记录，id 小于或等于 2 的记录不会被修改。

总结 :
这条语句会把 customer 表中所有 id 大于 2 的行的 created_at 字段更新为当前时间戳。


- Dockerfile
1. download java jdk 21, delete except 
2. IntelliJ IDEA => Project Structure
- SpringBoot terminal : 
4. mvn package -DskipTests -X
5. cd target/
6. java -jar SpringBoot3-0.0.1-SNAPSHOT.jar 
7. ssh ...
8. cd ~/Desktop/SpringBoot3
9. ls
10. ls -la start-dev.sh
11. SECRET=newsecretyooooo ./start-dev.sh
12. error => FROM amazoncorretto:21-alpine
13. docker ps | grep 8000 => [::]:8000->8000/tcp   springbootcontainer
14. docker logs springbootcontainer
15. docker logs springbootcontainer -f 


# SpringBoot3App

This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 15.2.11.

## Development server

Run `ng serve` for a dev server. Navigate to `http://localhost:4200/`. The application will automatically reload if you change any of the source files.

## Code scaffolding

Run `ng generate component component-name` to generate a new component. You can also use `ng generate directive|pipe|service|class|guard|interface|enum|module`.

## Build

Run `ng build` to build the project. The build artifacts will be stored in the `dist/` directory.

## Running unit tests

Run `ng test` to execute the unit tests via [Karma](https://karma-runner.github.io).

## Running end-to-end tests

Run `ng e2e` to execute the end-to-end tests via a platform of your choice. To use this command, you need to first add a package that implements end-to-end testing capabilities.

## Further help

To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI Overview and Command Reference](https://angular.io/cli) page.
