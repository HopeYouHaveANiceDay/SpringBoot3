import { DataState } from "../enum/datastate.enum";
import { Customer } from "./customer";
import { Events } from "./event";
import { Role } from "./role";
import { User } from "./user";

/*
appstates.ts 的程式碼主要是 定義前端的資料型別 (interfaces & enums)，
讓 Angular/TypeScript 在處理 API 回應或應用狀態時更安全、更一致。
*/


// checking the login state
export interface LoginState {
    dataState: DataState;
    loginSuccess?: boolean;
    error?: string;
    message?: string;
    isUsingMfa?: boolean;
    phone?: string; //send them a text message
}

// 這段程式碼定義的是一個 ( <T> ) TypeScript 泛型介面 (Generic Interface)，用來描述後端 API 回傳的標準格式。
//這個 CustomhttpResponse<T> 是一個 通用 API 回應格式，用泛型 T 來包裝不同型別的資料，並且提供標準欄位（時間戳記、狀態碼、訊息等），讓前端在處理 API 回應時更一致、更安全。
export interface CustomHttpResponse<T> {
    timestamp: Date;
    statusCode: number;
    status: string;
    message: string;
    reason?: string; //可選屬性 (?) 代表有些回應可能不需要這些欄位。
    developerMessage?: string;
    data?: T;
/*
(1) 泛型 T
    讓 data 欄位可以根據不同 API 回傳不同型別。
    例如：
    登入 API → data 可能是 User 物件。
    列表 API → data 可能是 User[] 陣列。

(2) 可選屬性 (?)
    reason, developerMessage, data 都是可選的，代表有些回應可能不需要這些欄位。
*/
}

// this profile is not the user, is the Profile page
// 這段程式碼定義了一個 Profile 介面，用來描述使用者的登入狀態與憑證。
export interface Profile {
    user: User; // 通常會包含使用者的基本資料
    events?: Events[];
    roles?: Role[];
    access_token?: string; // 短期有效的 Token，通常由後端 (例如 Spring Boot + JWT) 發給前端。
    refresh_token: string; //長期有效的 Token，用來在 access_token 過期時換取新的 access_token。
}

/* come from the console log in Chrome inspect

你問到為什麼要把 Page 介面從這樣：
    export interface Page {
      content: Customer[];
      totalPages: number;
      totalElements: number;
      numberOfElements: number;
      size: number;
      number: number;
    }

改成這樣：
    export interface Page<T> {
      content: T[];
      totalPages: number;
      totalElements: number;
      numberOfElements: number;
      size: number;
      number: number;
    }

原因解釋 :
1. 原本的設計只能固定存 Customer
      在舊版 Page 裡，content 永遠是 Customer[]。
      這代表這個分頁物件只能用來表示「客戶清單的分頁」，不能用來表示其他資料型別（例如 Invoice）。

2. 實際需求有不同型別的分頁
      你在專案裡有多個 API：
          searchCustomers$ → 回傳 Page<Customer>
          invoices$ → 回傳 Page<Invoice>
      如果 Page 只能固定是 Customer[]，那在處理 Invoice 的分頁時就不合適。

3. 泛型設計更靈活
      改成 Page<T> 之後，content 可以是任何型別的陣列。
      例如：
          Page<Customer>   // 客戶分頁
          Page<Invoice>    // 發票分頁
          Page<User>       // 使用者分頁
      這樣一個介面就能重複使用，不需要為每種資料型別都寫一個新的分頁介面。

4. 符合後端回傳結構
      後端 Spring Boot 的分頁回傳通常是泛型，例如：
          Page<Customer>
          Page<Invoice>
      前端用 Page<T> 就能對應後端的泛型設計，保持一致。

✅ 總結
    舊版 Page 只能表示「客戶分頁」，不夠通用。
    改成 Page<T> 之後，content 可以是任何型別的陣列，讓同一個介面能處理不同資料的分頁。
    這樣設計更靈活、可重用，並且和後端的泛型分頁設計保持一致。         */
export interface Page<T> {
    content: T[];
    totalPages: number;
    totalElements: number;
    numberOfElements: number;
    size: number;
    number: number;
  }

export interface CustomerState {
    user: User;
    customer: Customer;
}

export interface RegisterState {
    dataState: DataState;
    registerSuccess?: boolean;
    error?: string;
    message?: string;
}


export type AccountType = 'account' | 'password';

export interface VerifyState {
    dataState: DataState;
    verifySuccess?: boolean;
    error?: string;
    message?: string;
    title?: string;//there is success or error
    type?: AccountType; //type can be an account or a password
}

