package com.werryxgames.messaje;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.ui.Value.Fixed;
import com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
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
  public long currentUser = 0;
  public boolean changedUser = false;
  ArrayList<FormattedMessage> formattedMessages = new ArrayList<>(64);
  ArrayList<Message> allMessages;
  ArrayList<User> users;
  Table messagesTable;
  Table usersPaneContainer;
  Table sendMessageTable;
  TextField messageArea;
  ScrollPane messagesPane;
  Table usersTable;
  Runnable unblockFunction;
  String addLogin;

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

    this.messagesTable = new Table();
    this.messagesTable.left().top();
    this.messagesTable.pack();

    this.messagesPane = new ScrollPane(this.messagesTable);
    messagesPane.setOverscroll(false, false);
    messagesPane.setScrollingDisabled(true, false);
    messagesPane.setFillParent(true);

    Table msgTable = new Table();
    msgTable.add(messagesPane).left().top().fillX();
    msgTable.pack();

    this.usersPaneContainer = new Table();
    this.usersPaneContainer.pack();
    TextureRegionDrawable usersBackground = this.colorToDrawable(new Color(0x191919ff));
    this.usersPaneContainer.setBackground(usersBackground);
    Table table = new Table();
    table.center().left().add(this.usersPaneContainer).width(300)
        .height(Value.percentHeight(1f, table));
    table.add(msgTable).fillX().height(Value.percentHeight(1f, table));
    table.pack();
    table.setFillParent(true);
    table.setBackground(this.colorToDrawable(new Color(0x121212ff)));
    this.game.stage.addActor(table);

    this.sendMessageTable = new Table();
    this.messageArea = new TextField("",
        UiStyle.getTextFieldStyle(this.game.fontManager, 0, 24));
    sendMessageTable.left().add(messageArea).expandX().fillX();
    TextButton sendMessageButton = new TextButton("Send",
        UiStyle.getTextButtonStyle(this.game.fontManager, 0, 24));
    sendMessageButton.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        if (!ContactsScreen.this.changedUser) {
          return;
        }

        String text = messageArea.getText();
        byte[] messageBytes = text.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(2 + 8 + 2 + messageBytes.length);
        buffer.putShort((short) 3);
        buffer.putLong(ContactsScreen.this.currentUser);
        buffer.putShort((short) messageBytes.length);
        buffer.put(messageBytes);
        ContactsScreen.this.game.client.send(buffer);
        messageArea.setText("");

        Table table = new Table();
        table.center().right();
        ContactsScreen.this.formattedMessages.add(
            new FormattedMessage(new Message(0, ContactsScreen.this.currentUser, true, text),
                table));
        ContactsScreen.this.reformatMessages();
      }
    });

    final int width = Gdx.graphics.getWidth();

    sendMessageTable.add(sendMessageButton).padLeft(4);
    sendMessageTable.pack();
    sendMessageTable.setPosition(308, 8);
    sendMessageTable.setWidth(width - 316);
    sendMessageTable.setHeight(40);
    this.messageArea.setWidth(width - 362);
    this.game.stage.addActor(sendMessageTable);
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

    this.messagesTable.add(messagesContainer).padBottom(56);
    this.messageArea.setWidth(width - 388);
    this.sendMessageTable.setWidth(width - 316);
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
        new Fixed(width - 396 - 18));
    table.pack();

    if (message.sentByMe) {
      table.setBackground(new NinePatchDrawable(new NinePatch(ResourceLoader.loadTexture(
          Gdx.files.internal("icons" + File.separator + "sent_message.png")), 19, 15, 20, 19)));
    } else {
      table.setBackground(new NinePatchDrawable(new NinePatch(ResourceLoader.loadTexture(
          Gdx.files.internal("icons" + File.separator + "received_message.png")), 19, 15, 20, 19)));
    }

    containerTable.add(table).maxWidth(width - 346 - 18);
    containerTable.pack();
    return containerTable;
  }

  public void updateUsers() {
    TextButtonStyle textButtonStyle = new TextButtonStyle();
    textButtonStyle.font = this.game.fontManager.getFont(0, 24);
    textButtonStyle.up = new BaseDrawable();
    textButtonStyle.down = new BaseDrawable();
    textButtonStyle.focused = new BaseDrawable();
    textButtonStyle.disabled = new BaseDrawable();
    textButtonStyle.over = new BaseDrawable();

    this.usersTable.clear();
    int usersCount = this.users.size();

    for (int i = 0; i < usersCount; i++) {
      User user = this.users.get(i);
      TextButton button = new TextButton(user.name, textButtonStyle);
      long contactId = user.id;
      button.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          ContactsScreen.this.formattedMessages.clear();

          for (Message message : ContactsScreen.this.allMessages) {
            if (message.contactId != contactId) {
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
          ContactsScreen.this.currentUser = contactId;
          ContactsScreen.this.changedUser = true;
        }
      });
      usersTable.add(button).width(300 - 18).height(40);
      usersTable.row();
    }

    TextButton button = new TextButton("<Add new>", textButtonStyle);
    button.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        WindowStyle windowStyle = new WindowStyle(
            ContactsScreen.this.game.fontManager.getFont(0, 32), new Color(0xdededeff),
            ContactsScreen.this.colorToDrawable(new Color(0x00000080)));
        Dialog dialog = new Dialog("", windowStyle);
        dialog.clear();
        dialog.setFillParent(true);
        dialog.add(new Label("Add new contact by login",
                UiStyle.getLabelStyle(ContactsScreen.this.game.fontManager, 0, 32))).colspan(2)
            .padBottom(8);
        dialog.row();
        dialog.add(new Label("Login:",
            UiStyle.getLabelStyle(ContactsScreen.this.game.fontManager, 0, 24)));
        TextField loginField = new TextField("",
            UiStyle.getTextFieldStyle(ContactsScreen.this.game.fontManager, 0, 24));
        dialog.add(loginField).fillX();
        dialog.row();
        Table buttonsTable = new Table();
        TextButton closeButton = new TextButton("Close",
            UiStyle.getTextButtonStyle(ContactsScreen.this.game.fontManager, 0, 24));
        closeButton.addListener(new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            dialog.hide();
          }
        });
        buttonsTable.add(closeButton).fillX().padRight(6).width(200);
        TextButton addContactButton = new TextButton("Add",
            UiStyle.getTextButtonStyle(ContactsScreen.this.game.fontManager, 0, 24));
        addContactButton.addListener(new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            ContactsScreen.this.unblockFunction = dialog::hide;

            closeButton.setDisabled(true);
            addContactButton.setDisabled(true);
            String addLogin = loginField.getText();
            byte[] loginBytes = addLogin.getBytes(StandardCharsets.UTF_8);
            ContactsScreen.this.addLogin = addLogin;
            ContactsScreen.this.game.client.sendBlocking(
                ByteBuffer.allocate(2 + 1 + loginBytes.length).putShort((short) 4)
                    .put((byte) loginBytes.length).put(loginBytes));
          }
        });
        buttonsTable.add(addContactButton).fillX().padLeft(6).width(200);
        dialog.add(buttonsTable).colspan(2).padTop(8);
        dialog.show(ContactsScreen.this.game.stage);
      }
    });

    usersTable.add(button).width(300 - 18).height(40);
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

      this.usersTable = new Table();
      this.updateUsers();

      usersTable.pack();
      ScrollPane usersPane = new ScrollPane(usersTable, UiStyle.getScrollPaneStyle());
      usersPane.setOverscroll(false, false);
      usersPane.setFillParent(true);
      Table table2 = new Table();
      this.usersPaneContainer.left().add(usersPane).width(300);
    } else if (code == 8) {
      long userId = serverMessage.getLong();

      for (User user : this.users) {
        if (user.id == userId) {
          this.unblockFunction.run();
          this.game.logger.warning("User already added");
          return;
        }
      }

      this.users.add(new User(userId, this.addLogin));
      this.updateUsers();
      this.currentUser = userId;
      // TODO: Fix messages not showing if there are too few messages
      this.unblockFunction.run();
    } else if (code == 9) {
      // TODO: Add dialogs on errors/warnings
      this.game.logger.warning("User not found");
      this.unblockFunction.run();
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
