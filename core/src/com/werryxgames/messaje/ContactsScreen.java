package com.werryxgames.messaje;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.ui.Value.Fixed;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import java.nio.ByteBuffer;
import java.util.Locale;

public class ContactsScreen extends DefaultScreen {
  /**
   * Default constructor for {@code DefaultScreen}.
   *
   * @param game Instance of {@link Messaje}.
   * @since 1.0
   */
  public ContactsScreen(Messaje game) {
    super(game);
  }

  @Override
  void onUpdate(float delta) {

  }

  @Override
  void onResize(int width, int height) {

  }

  @Override
  public void init() {
    this.game.client.send(ByteBuffer.allocate(2).putShort((short) 2));

    Table table2 = new Table();
    Table usersTable = new Table();
    TextButtonStyle textButtonStyle = new TextButtonStyle();
    textButtonStyle.font = this.game.fontManager.getFont(0, 24);
    textButtonStyle.up = new BaseDrawable();
    textButtonStyle.down = new BaseDrawable();
    textButtonStyle.focused = new BaseDrawable();
    textButtonStyle.disabled = new BaseDrawable();
    textButtonStyle.over = new BaseDrawable();
    for (int i = 0; i < 100; i++) {
      TextButton button = new TextButton("User: " + i, textButtonStyle);
      int finalI = i;
      button.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          ContactsScreen.this.game.logger.fine("Selected user: " + finalI);
        }
      });
      usersTable.add(button).width(300 - 18).height(40);
      usersTable.row();
    }
    TextureRegionDrawable usersBackground = this.colorToDrawable(new Color(0x191919ff));
    usersTable.setBackground(usersBackground);
    usersTable.pack();
    ScrollPane usersPane = new ScrollPane(usersTable, UiStyle.getScrollPaneStyle());
    usersPane.setOverscroll(false, false);
    usersPane.setFillParent(true);
    table2.left().add(usersPane);
    table2.pack();
    table2.setBackground(usersBackground);

    Table table3 = new Table();
    table3.left();
    // TODO: Re-format in onResize()
    FormattedText.formatLabels(table3, "Test **bold** *italic* _sup_ normal. TEST 2 VERY LONG TEXT *WWWWWWWWWWWWWWWWWW* **WMWMWMWMWMWMWMMWMWMWMMWMWMWM*WMWMMWMWWMWMMWMWMWMWMMWM*.**", 400, 100, Color.WHITE, this.game.fontManager, 0, 16,
        new Fixed(Gdx.graphics.getWidth() - 300));
    table3.pack();

    Table table = new Table();
    table.center().left().add(table2).width(300).height(Value.percentHeight(1f, table));
    table.add(table3).expandX().height(Value.percentHeight(1f, table));
    table.pack();
    table.setFillParent(true);
    table.setBackground(this.colorToDrawable(new Color(0x121212ff)));
    table.setDebug(true, true);

    this.game.stage.addActor(table);
  }

  @Override
  public void onMessage(int code, ByteBuffer serverMessage) {
    if (code == 7) {
      int messagesCount = serverMessage.getInt();
      Message[] messages = new Message[messagesCount];

      for (int i = 0; i < messagesCount; i++) {
        messages[i] = new Message().fromBytes(serverMessage);
      }

      for (Message message : messages) {
        this.game.logger.fine(String.format(Locale.ENGLISH, "Message. Id: %d, contact id: %d, sent by me: %s, text: '%s'", message.id, message.contactId, message.sentByMe ? "true" : "else", message.text));
      }
    }
  }

  @Override
  public void onDisconnect() {

  }

  @Override
  public void show() {

  }

  @Override
  public void pause() {

  }

  @Override
  public void resume() {

  }

  @Override
  public void hide() {

  }

  @Override
  public void onDispose() {
  }
}
