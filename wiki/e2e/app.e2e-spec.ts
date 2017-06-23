import { WikiPage } from './app.po';

describe('wiki App', () => {
  let page: WikiPage;

  beforeEach(() => {
    page = new WikiPage();
  });

  it('should display welcome message', done => {
    page.navigateTo();
    page.getParagraphText()
      .then(msg => expect(msg).toEqual('Welcome to app!!'))
      .then(done, done.fail);
  });
});
