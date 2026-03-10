// loading => making http request to the backend, request some data
// loaded => get a response
// error => get any kind of errors

// keep  checking the login state
export enum DataState {
    LOADING = 'LOADING_STATE', LOADED = 'LOADED_STATE', ERROR = 'ERROR_STATE'
}

