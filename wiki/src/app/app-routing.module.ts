import {NgModule} from '@angular/core';
import {RouterModule, Routes} from "@angular/router";
import {APP_BASE_HREF} from "@angular/common";
import {EditorComponent} from "./editor/editor.component";
import {ArticlesComponent} from "./articles/articles.component";
import {ParticipateComponent} from "./participate/participate.component";
import {PlayComponent} from "./play/play.component";
import {ArticleComponent} from "./editor/article/article.component";
import {AuthGuard} from "./auth.guard";
import {PricingComponent} from "./pricing/pricing.component";
import {HomeComponent} from "./home/home.component";
import {PassportComponent} from "./passport/passport.component";
import {PageNotFoundComponent} from "./page-not-found.component";
import {SettingsComponent} from "./settings/settings.component";

const routes: Routes = [
  {
    path: '',
    redirectTo: '/home',
    pathMatch: 'full'
  },
  {
    path: 'home', component: HomeComponent
  },
  {
    path: 'articles', component: ArticlesComponent,
    children: [
      {
        path: 'article/:id', component: ArticleComponent
      }
    ]
  },
  {
    path: 'editor',
    component: EditorComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'pricing',
    component: PricingComponent
  },
  {
    path: 'participate',
    component: ParticipateComponent
  },
  {
    path: 'play',
    component: PlayComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'passport',
    component: PassportComponent,
  },
  {
    path: 'settings',
    component: SettingsComponent
  },
  {
    path: '**',
    component: PageNotFoundComponent
  }
];
@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
  providers: [
    {provide: APP_BASE_HREF, useValue: '/'}
  ]
})
export class AppRoutingModule {
}
