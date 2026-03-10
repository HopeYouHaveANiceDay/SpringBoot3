import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpResponse
} from '@angular/common/http';
import { Observable, of, tap } from 'rxjs';
import { HttpCacheService } from '../service/http.cache.service';

//it is a frontend Coach

/* 1. First key =>  http://localhost:8080/customer/get/1
      => the server makes the call, and then the call get interceptor, and then return the Observable of HttpResponse to the server because the component cammunicates with the server, not interceptor

  2. second key is the first page  => http://localhost:8080/customer/list?page=0


If update the information such as customer profile
=> Clearing entire cache
    */

/*
這段程式碼是一個在 Angular 專案中建立的 HTTP Interceptor，名稱是 TokenInterceptor。
它的主要用途是：
    在每次 HTTP 請求發送之前，攔截並檢查是否需要使用快取，或是否需要清除快取。

總結 :
TokenInterceptor 的功能是：
  1. 攔截所有 HTTP 請求。
  2. 對登入、註冊、驗證等敏感操作直接放行，不快取。
  3. 對非 GET 或下載報表的請求，清除快取並放行。
  4. 對 GET 請求，先檢查快取，有的話直接回傳快取。
  5. 如果沒有快取，發送請求並在回應後存入快取。

這樣設計的好處是：
    減少重複 API 請求，提高效能。
    保證敏感操作與即時資料不會被快取。
    提供一個簡單的快取機制，方便管理。
 */

//@Injectable({ providedIn: 'root' })

// 宣告這是一個可注入的服務。
// 這裡沒有指定 providedIn: 'root'，代表需要在 AppModule 或其他模組的 providers 中手動註冊。
// "app.module.ts" add providers
@Injectable()

// 宣告一個類別並實作 CachInterceptor 介面。攔截器的核心方法是 intercept()。
export class CacheInterceptor implements HttpInterceptor {

  // 透過 依賴注入 使用 HttpCacheService，來管理 HTTP 回應快取。
  constructor(private httpCache: HttpCacheService) {}

  //這個方法會在每次 HTTP 請求發送前被呼叫。
  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> | Observable<HttpResponse<unknown>> { // <unknown> = any type of Http Response

    // 1. 特殊路徑直接放行
    //    如果 URL 包含登入、註冊、驗證、刷新 Token、重設密碼等字樣，直接放行，不使用快取
    if(request.url.includes('verify') || request.url.includes('login') || request.url.includes('register')
          || request.url.includes('refresh') || request.url.includes('resetpassword') || request.url.includes('verify') || request.url.includes('new/password')) {
              return next.handle(request); //let the request go through
      }

      //2. 非 GET 請求或下載 Excel 報表
      //    如果不是 GET 請求，或是下載 Excel 報表，則清除所有快取並放行。
      //    這樣避免快取到需要即時更新的資料。
      // request.url.includes('download') means that click  excel icon and then download Excel Report
      if(request.method !== 'GET' || request.url.includes('download')) {
          this.httpCache.evictAll();
          //this.httpCache.evict(request.url);
          return next.handle(request); //let the request go through
      }

      // 3. 嘗試從快取取資料
      //     檢查快取中是否已經有這個 URL 的回應。
      //     如果有，直接回傳快取的結果，不再發送新的 HTTP 請求。
      const cachedResponse: HttpResponse<any> = this.httpCache.get(request.url);
      if(cachedResponse) {
        console.log(`Found Response in Cache`, cachedResponse);
        this.httpCache.logCache();
        return of(cachedResponse); //return Observerable
      }
      //4. 沒有快取 → 呼叫 handleRequestCache
      //   如果快取不存在，則繼續發送請求，並在回應後決定是否要快取。
      return this.handleRequestCache(request, next);
  }

  //handleRequestCache 方法
  // 發送請求並監聽回應。
  // 如果回應是 HttpResponse，而且請求不是 DELETE，就把回應存入快取。
  // 下次再請求相同的 URL，就能直接從快取取出。
  private handleRequestCache(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(request)
    .pipe(
      tap(response => {
        if(response instanceof HttpResponse && request.method !== 'DELETE') {
          console.log('Caching Response', response);
          this.httpCache.put(request.url, response);
        }
      })
    );
  }
}
