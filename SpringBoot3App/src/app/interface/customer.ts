import { Invoice } from "./invoice";

//the representation of the shape of the Customer whatever coming from the backend to fronend
export interface Customer {
    id: number;
    name: string;
    email: string;
    address: string;
    type: string;
    status: string;
    imageUrl: string;
    phone: string;
    createdAt: Date;
    invoices?: Invoice[]; // ? => optional because they may do not have invoices at the particular time
}
