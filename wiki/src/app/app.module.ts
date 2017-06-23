import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {HttpModule} from '@angular/http';

import {AppComponent} from './app.component';
import {DraughtComponent} from './editor/board/draught/draught.component';
import {BoardComponent} from './editor/board/board.component';
import {SquareComponent} from './editor/board/square/square.component';
import {BoardService} from "./service/board.service";
import {SocketService} from "./service/socket.service";
import {Ng2Webstorage} from "ngx-webstorage";
import {BoardToolbarComponent} from './editor/board-toolbar/board-toolbar.component';
import {DraughtEditComponent} from './editor/board-toolbar/draught-edit/draught-edit.component';
import {HeaderComponent} from './header/header.component';
import {FooterComponent} from './footer/footer.component';
import {EditorComponent} from './editor/editor.component';
import {DraughtMoveDirective} from "./directives/draught-move.directive";
import {ArticleComponent} from './editor/article/article.component';
import {CreateArticleDialogComponent} from "./editor/dialogs/create-article-dialog/create-article-dialog.component";
import {ArticleService} from "./service/article.service";
import {OpenArticleDialogComponent} from './editor/dialogs/open-article-dialog/open-article-dialog.component';
import {BlockUIModule} from "ng-block-ui";
import { ArticlesComponent } from './articles/articles.component';
import { ParticipateComponent } from './participate/participate.component';
import { PlayComponent } from './play/play.component';
import {AppRoutingModule} from "./app-routing.module";
import { MostRecentArticlesComponent } from './home/most-recent-articles/most-recent-articles.component';
import { PricingComponent } from './pricing/pricing.component';
import { HomeComponent } from './home/home.component';
import { PassportComponent } from './passport/passport.component';
import {UserService} from "./service/user.service";
import {PageNotFoundComponent} from "./page-not-found.component";
import { SettingsComponent } from './settings/settings.component';
import {AuthGuard} from "./auth.guard";
import {requestOptionsProvider} from "./service/default-request-options";

@NgModule({
  declarations: [
    AppComponent,
    PageNotFoundComponent,
    DraughtComponent,
    BoardComponent,
    SquareComponent,
    BoardToolbarComponent,
    DraughtEditComponent,
    HeaderComponent,
    FooterComponent,
    EditorComponent,
    DraughtMoveDirective,
    ArticleComponent,
    CreateArticleDialogComponent,
    OpenArticleDialogComponent,
    ArticlesComponent,
    ParticipateComponent,
    PlayComponent,
    MostRecentArticlesComponent,
    PricingComponent,
    HomeComponent,
    PassportComponent,
    SettingsComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpModule,
    Ng2Webstorage,
    Ng2Webstorage.forRoot({prefix: 'shashki', separator: '.', caseSensitive: true}),
    BlockUIModule,
    AppRoutingModule
  ],
  providers: [
    AuthGuard,
    ArticleService,
    BoardService,
    SocketService,
    UserService,
    requestOptionsProvider
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
