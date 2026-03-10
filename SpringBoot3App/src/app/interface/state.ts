import { DataState } from "../enum/datastate.enum";

// This state is going to represent an application-wide state.
export interface State<T> {
    dataState: DataState;
    appData?: T; 
    error?: string;
}

/*
📖 詳細解釋
import { DataState } from "../enum/datastate.enum";
    從 enum/datastate.enum.ts 匯入一個列舉型別 DataState。
    這個 enum 通常會定義幾種狀態，例如：LOADING, LOADED, ERROR，用來表示資料目前的狀態。
export interface State<T> { ... }
    宣告一個泛型介面 State<T>。
    泛型 T 表示這個介面可以包裝任何型別的資料，例如 User、Role、Product。
    這樣設計的好處是：同一個狀態介面可以重複使用在不同資料型別上。
dataState: DataState;
    必填欄位，型別是 DataState。
    用來表示目前資料的狀態，例如正在載入、已載入、或錯誤。
appData?: T;
    可選欄位（? 表示可以不存在）。
    型別是泛型 T，代表實際的應用資料。
    例如如果 T = User，那麼 appData 就是 User 型別的物件。
error?: string;
    可選欄位，用來存放錯誤訊息。
    當 dataState = ERROR 時，這裡通常會有錯誤描述。
*/
