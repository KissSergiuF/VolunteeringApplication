import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomePageComponent } from './component/home-page/home-page.component';
import { AboutUsComponent } from './component/about-us/about-us.component';
import { ContactUsComponent } from './component/contact-us/contact-us.component';
import { MapComponent } from './component/map/map.component';
import { LoginComponent } from './component/login/login.component';
import { ProfilComponent } from './component/profil/profil.component';
import { ChatComponent } from './component/chat/chat.component';
import { ArchivedEventsComponent } from './component/archived-events/archived-events.component';
import { PublicProfileComponent } from './component/public-profile/public-profile.component';
const routes: Routes = [
  { path: '', redirectTo: '/home', pathMatch: 'full' },
  { path: 'home', component: HomePageComponent },
  { path: 'about-us', component: AboutUsComponent},
  { path: 'contact-us', component: ContactUsComponent},
  { path: 'map', component: MapComponent},
  { path: 'login', component: LoginComponent},
  { path: 'profil', component: ProfilComponent},
  { path: 'chat/:eventId', component: ChatComponent },
  { path: 'archived-events', component: ArchivedEventsComponent },
  { path: 'public-profile/:userId', component: PublicProfileComponent },

];


@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
