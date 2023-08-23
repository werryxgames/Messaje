package com.werryxgames.messaje;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
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

/**
 * Class, where all known contacts and messages from selected contact are listed.
 *
 * @since 1.0
 */
public class ContactsScreen extends DefaultScreen {

  public ConcurrentLinkedQueue<Runnable> networkHandlerQueue = new ConcurrentLinkedQueue<>();
  ArrayList<FormattedMessage> formattedMessages = new ArrayList<>(64);
  ArrayList<Message> allMessages;
  ArrayList<User> users;
  Table messagesTable;
  Table usersPaneContainer;

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
    this.reformatMessages(width, height);
  }

  @Override
  public void init() {
    this.game.client.send(ByteBuffer.allocate(2).putShort((short) 2));

    // TODO: Add scrollbar
    this.messagesTable = new Table();
    this.messagesTable.left().top();
    this.messagesTable.pack();

    this.usersPaneContainer = new Table();
    this.usersPaneContainer.pack();
    TextureRegionDrawable usersBackground = this.colorToDrawable(new Color(0x191919ff));
    this.usersPaneContainer.setBackground(usersBackground);
    Table table = new Table();
    table.center().left().add(this.usersPaneContainer).width(300)
        .height(Value.percentHeight(1f, table));
    table.add(this.messagesTable).expandX().height(Value.percentHeight(1f, table));
    table.pack();
    table.setFillParent(true);
    table.setBackground(this.colorToDrawable(new Color(0x121212ff)));
    this.game.stage.addActor(table);
  }

  /**
   * Reformats simplified non-standardized markup language to formatted text from all known
   * messages.
   *
   * @param width Current width of window.
   */
  public void reformatMessages(int width, int height) {
    if (this.messagesTable == null) {
      return;
    }

    this.messagesTable.clear();
    Table messagesContainer = new Table();
    messagesContainer.left().top();

    for (FormattedMessage formattedMessage : this.formattedMessages) {
      this.reformatMessage(formattedMessage.message, formattedMessage.table);
      messagesContainer.add(formattedMessage.table).width(width - 312).padLeft(5).padRight(5)
          .padTop(5).padBottom(5);
      messagesContainer.row();
    }

    this.messagesTable.add(messagesContainer).height(height - 56);

    Table sendMessageTable = new Table();
    TextField messageArea = new TextField("",
        UiStyle.getTextFieldStyle(this.game.fontManager, 0, 24));
    sendMessageTable.add(messageArea).width(width - 380);
    TextButton sendMessageButton = new TextButton("Send",
        UiStyle.getTextButtonStyle(this.game.fontManager, 0, 24));
    sendMessageButton.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        System.out.println("Sending message...");
        // TODO: Send message
        messageArea.setText("");
      }
    });
    sendMessageTable.add(sendMessageButton).padLeft(4);
    sendMessageTable.pack();
    this.messagesTable.row();
    this.messagesTable.add(sendMessageTable).width(width - 316).height(40).padLeft(8).padRight(8)
        .padTop(8).padBottom(8);
  }

  public void reformatMessages() {
    this.reformatMessages(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
  }

  /**
   * Formats simplified non-standardized markup language to formatted text.
   *
   * @param message      Message to format.
   * @param messageTable Table of message to format.
   * @return Table with formatted labels.
   */
  public Table reformatMessage(Message message, Table messageTable) {
    Table containerTable;
    containerTable = messageTable;
    containerTable.clear();

    Table table = new Table();
    int width = Gdx.graphics.getWidth();

    FormattedText.formatLabels(table, message.text, Color.WHITE, this.game.fontManager, 0, 24,
        new Fixed(width - 396));
    table.pack();

    if (message.sentByMe) {
      table.setBackground(new NinePatchDrawable(new NinePatch(ResourceLoader.loadTexture(
          Gdx.files.internal("icons" + File.separator + "sent_message.png")), 19, 15, 20, 19)));
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

  protected void onMessageMain(int code, ByteBuffer serverMessage) {
    if (code == 7) {
      int usersCount = serverMessage.getInt();
      this.users = new ArrayList<>(usersCount);

      for (int i = 0; i < usersCount; i++) {
        User user = new User().fromBytes(serverMessage);
        this.users.add(user);
      }

      int messagesCount = serverMessage.getInt();
      this.allMessages = new ArrayList<>(messagesCount);

      for (int i = 0; i < messagesCount; i++) {
        Message message = new Message().fromBytes(serverMessage);
        boolean added = false;

        for (int j = 0; j < this.allMessages.size(); j++) {
          Message existingMessage = this.allMessages.get(j);

          if (existingMessage.id > message.id) {
            this.allMessages.add(j, message);
            added = true;
            break;
          }
        }

        if (!added) {
          this.allMessages.add(message);
        }
      }

      for (Message message : this.allMessages) {
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
            message.contactId, message.sentByMe ? "true" : "false", message.text));
      }

      Table usersTable = new Table();
      TextButtonStyle textButtonStyle = new TextButtonStyle();
      textButtonStyle.font = this.game.fontManager.getFont(0, 24);
      textButtonStyle.up = new BaseDrawable();
      textButtonStyle.down = new BaseDrawable();
      textButtonStyle.focused = new BaseDrawable();
      textButtonStyle.disabled = new BaseDrawable();
      textButtonStyle.over = new BaseDrawable();

      for (int i = 0; i < usersCount; i++) {
        User user = this.users.get(i);
        TextButton button = new TextButton(user.name, textButtonStyle);
        int finalI = i + 1;
        button.addListener(new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            ContactsScreen.this.formattedMessages.clear();

            for (Message message : ContactsScreen.this.allMessages) {
              if (message.contactId != finalI) {
                continue;
              }

              Table table = new Table();
              table.center();

              if (message.sentByMe) {
                table.right();
              } else {
                table.left();
              }

              ContactsScreen.this.formattedMessages.add(new FormattedMessage(message, table));
            }

            ContactsScreen.this.reformatMessages();
          }
        });
        usersTable.add(button).width(300 - 18).height(40);
        usersTable.row();
      }

      usersTable.pack();
      ScrollPane usersPane = new ScrollPane(usersTable, UiStyle.getScrollPaneStyle());
      usersPane.setOverscroll(false, false);
      usersPane.setFillParent(true);
      Table table2 = new Table();
      this.usersPaneContainer.left().add(usersPane).width(300);
      this.reformatMessages();
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
