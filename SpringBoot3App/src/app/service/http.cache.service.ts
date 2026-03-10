import { Injectable } from "@angular/core";
import { HttpResponse } from "@angular/common/http";


/*
程式碼結構 :

1. @Injectable({ providedIn: 'root' })
      這表示這個 Service 可以在整個應用程式中使用，不需要額外在 providers 裡註冊。

2. private httpResponseCache: { [key: string]: HttpResponse<any> } = {};
      宣告一個物件，用來存放快取資料。
      key 是字串（通常代表 API 的 URL 或請求識別碼）。
      value 是對應的 HttpResponse。
      例如：{ "customers": HttpResponseObject }


方法解釋
put(key, httpResponse)
把一個 HTTP 回應存入快取。
key 是識別碼，httpResponse 是回應物件。
例如：put("customers", response) → 把 response 存起來。

get(key)
根據 key 取出快取的回應。
如果沒有找到，回傳 null 或 undefined。

evict(key)
刪除指定 key 的快取。
回傳布林值，表示刪除是否成功。

evictAll()
清空整個快取。
會在 console 顯示「Clearing entire cache」。

logCache()
把目前快取內容輸出到 console，方便除錯。

總結
HttpCacheService 提供了：
新增快取 (put)
讀取快取 (get)
刪除單筆快取 (evict)
清空快取 (evictAll)
檢視快取 (logCache)
它是一個簡單但實用的工具，讓 Angular 應用程式可以更有效率地管理 HTTP 回應。
要不要我再幫你示範一個 實際用法範例，例如在呼叫 API 的時候先檢查快取，有的話

*/


@Injectable()

export class HttpCacheService {
  // The key is an array becuase we want to store multiple keys with their Response.
  private httpResponseCache: { [key: string]: HttpResponse<any> } = {};
  //cacheDate = { 1=> value1, 2=> value2}

  put = (key: string, httpResponse: HttpResponse<any>): void => {
    console.log(`Caching response`, httpResponse);
    this.httpResponseCache[key] = httpResponse;
  }

  get = (key: string): HttpResponse<any> | null | undefined => this.httpResponseCache[key];

  evict = (key: string): boolean => delete this.httpResponseCache[key];

  evictAll = (): void => {
    console.log(`Clearing entire cache`);
    this.httpResponseCache = {};
  }

  logCache = (): void => console.log(this.httpResponseCache);
}
