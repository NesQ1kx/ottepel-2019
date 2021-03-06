import { Injectable } from '@angular/core';
import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from "@angular/common/http";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class InterceptorService implements HttpInterceptor{

  constructor() { }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
      if(localStorage.getItem('token') !== null) {
          request = request.clone({
              headers: request.headers.set('Authorization', localStorage.getItem('token'))
          })
      }

      return next.handle(request);
  }
}
