/*
是一個 TypeScript 介面 (interface) 宣告，
用來定義「角色 (Role)」物件的結構。

✅ 總結
Role 是一個介面，用來定義角色物件的結構。
它包含三個欄位：id (數字)、name (字串)、permission (字串)。
好處是讓程式碼更嚴謹，避免物件結構錯誤，並且在專案中統一角色資料的格式。

📖 詳細解釋
(1) export
      表示這個介面可以被其他檔案匯入使用。

      例如在 component 或 service 裡可以寫：
      import { Role } from '../interface/role';

      例如在 appstates.ts 裡可以寫：
      import { Role } from "./role";

(2) interface Role { ... }
      宣告一個名為 Role 的介面。
      介面是 TypeScript 用來描述物件結構的工具，確保物件必須符合指定的欄位和型別。
*/
export interface Role {
    id: number;
    name: string;
    permission: string;
}
