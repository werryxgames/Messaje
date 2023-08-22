package com.werryxgames.messaje;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.ui.Value.Fixed;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;

class FormattedMessage {

  public Message message;
  public Table table;

  public FormattedMessage() {
  }

  public FormattedMessage(Message message, Table table) {
    this.message = message;
    this.table = table;
  }
}

public class ContactsScreen extends DefaultScreen {

  public ConcurrentLinkedQueue<Runnable> networkHandlerQueue = new ConcurrentLinkedQueue<>();
  ArrayList<FormattedMessage> formattedMessages = new ArrayList<>(64);
  Table messagesTable;

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
    while (this.networkHandlerQueue.size() > 0) {
      this.networkHandlerQueue.poll().run();
    }
  }

  @Override
  void onResize(int width, int height) {
    this.reformatMessages(width);
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
    int width = Gdx.graphics.getWidth();

    // TODO: Add scrollbar
    this.messagesTable = new Table();
    this.messagesTable.left().top();
    this.messagesTable.pack();
    this.messagesTable.setDebug(true, true);

    Table table = new Table();
    table.center().left().add(table2).width(300).height(Value.percentHeight(1f, table));
    table.add(this.messagesTable).expandX().height(Value.percentHeight(1f, table));
    table.pack();
    table.setFillParent(true);
    table.setBackground(this.colorToDrawable(new Color(0x121212ff)));
//    table.setDebug(true, true);

    this.game.stage.addActor(table);
  }

  public void reformatMessages(int width) {
    if (this.messagesTable == null) {
      return;
    }

    this.messagesTable.clear();

    for (FormattedMessage formattedMessage : this.formattedMessages) {
      this.reformatMessage(formattedMessage.message, formattedMessage.table);
      this.messagesTable.add(formattedMessage.table).width(width - 312).padLeft(5).padRight(5)
          .padTop(5).padBottom(5);
      this.messagesTable.row();
    }
  }

  public Table reformatMessage(Message message, Table messagesTable) {
    Table containerTable;
    containerTable = messagesTable;
    containerTable.clear();

    Table table = new Table();
    int width = Gdx.graphics.getWidth();

    FormattedText.formatLabels(table, message.text, Color.WHITE, this.game.fontManager, 0, 24,
        new Fixed(width - 396));
    table.pack();

    if (message.sentByMe) {
      table.setBackground(new NinePatchDrawable(new NinePatch(ResourceLoader.loadTexture(
        Gdx.files.internal("icons" + File.separator + "sent_message.png")), 14, 16, 24, 15)));
    } else {
      table.setBackground(new NinePatchDrawable(new NinePatch(ResourceLoader.loadTexture(
          Gdx.files.internal("icons" + File.separator + "received_message.png")), 19, 15, 20, 19)));
    }

    containerTable.add(table).maxWidth(width - 346);
    containerTable.pack();
    return containerTable;
  }

  @Override
  public void onMessage(int code, ByteBuffer serverMessage) {
    this.networkHandlerQueue.add(() -> this.onMessageMain(code, serverMessage));
  }

  public void onMessageMain(int code, ByteBuffer serverMessage) {
    if (code == 7) {
      int messagesCount = serverMessage.getInt();
      Message[] messages = new Message[messagesCount + 1];

      for (int i = 0; i < messagesCount; i++) {
        messages[i] = new Message().fromBytes(serverMessage);
      }

      messages[messagesCount] = new Message(999, 2, false, "Hello!");

      for (Message message : messages) {
        Table table = new Table();
        table.center();

        if (message.sentByMe) {
          table.right();
        } else {
          table.left();
        }

        this.formattedMessages.add(new FormattedMessage(message, table));
        this.game.logger.fine(String.format(Locale.ENGLISH,
            "Message. Id: %d, contact id: %d, sent by me: %s, text: '%s'", message.id,
            message.contactId, message.sentByMe ? "true" : "else", message.text));
      }

      this.reformatMessages(Gdx.graphics.getWidth());
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
