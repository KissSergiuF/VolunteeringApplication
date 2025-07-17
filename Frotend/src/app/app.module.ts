import { NgModule } from '@angular/core';
import { BrowserModule, provideClientHydration, withEventReplay } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { HomePageComponent } from './component/home-page/home-page.component';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatMenuModule } from '@angular/material/menu';
import { MatCardModule } from '@angular/material/card';
import { AboutUsComponent } from './component/about-us/about-us.component';
import { RouterModule } from '@angular/router';
import { ContactUsComponent } from './component/contact-us/contact-us.component';
import { ReactiveFormsModule } from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { HttpClientModule } from '@angular/common/http';
import { MapComponent } from './component/map/map.component';
import { GoogleMapsModule } from '@angular/google-maps';
import { LoginComponent } from './component/login/login.component';
import { FormsModule } from '@angular/forms';
import { MatSelectModule } from '@angular/material/select';
import { ProfilComponent } from './component/profil/profil.component';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { ChatComponent } from './component/chat/chat.component';
import { ConfirmDialogComponent } from './component/confirm-dialog/confirm-dialog.component';
import { MatDialogModule } from '@angular/material/dialog';
import { ArchivedEventsComponent } from './component/archived-events/archived-events.component';
import { FeedbackModalComponent } from './component/feedback-modal/feedback-modal.component';
import { PublicProfileComponent } from './component/public-profile/public-profile.component';

@NgModule({
  declarations: [
    AppComponent,
    HomePageComponent,
    AboutUsComponent,
    ContactUsComponent,
    MapComponent,
    LoginComponent,
    ProfilComponent,
    ChatComponent,
    ConfirmDialogComponent,
    ArchivedEventsComponent,
    FeedbackModalComponent,
    PublicProfileComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    MatToolbarModule,
    MatIconModule,
    MatButtonModule,
    MatSidenavModule,
    MatMenuModule,
    MatCardModule,
    RouterModule,
    ReactiveFormsModule,
    BrowserAnimationsModule,
    MatFormFieldModule,
    MatInputModule,
    HttpClientModule,
    GoogleMapsModule,
    FormsModule,
    MatSelectModule,
    MatSnackBarModule,
    MatDialogModule,

  ],
  providers: [
    provideClientHydration(withEventReplay()),
    provideAnimationsAsync(),
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
