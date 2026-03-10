export interface User {
    id: number;
    firstName: string;
    lastName: string;
    email: string;
    //get rid of the password because we don't want to ever send that to the frontend or to caller
    address?: string;
    phone?: string;//電話號碼雖然由數字組成，但它不是拿來做數學計算的。電話號碼常常包含 +、-、空格、括號 等符號。例如：+852-1234-1234。如果用 int 或 long，這些符號就無法保存。
    title?: string;
    bio?: string;
    imageUrl?: string;
    enabled: boolean;
    notLocked: boolean;
    usingMfa: boolean;
    createdAt?: Date;
    roleName: string;
    permissions: string;
}
