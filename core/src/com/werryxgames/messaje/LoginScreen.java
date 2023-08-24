package com.werryxgames.messaje;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Screen for logging in to account.
 *
 * @since 1.0
 */
public class LoginScreen extends DefaultScreen {

  static final byte[] PEPPER = Utils.hexToBytes(Config.get("password.pepper", "69D029BE4D8E0C42"));

  /**
   * Default constructor for {@code DefaultScreen}.
   *
   * @param game Instance of {@link Messaje}.
   * @since 1.0
   */
  public LoginScreen(Messaje game) {
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
    Table table2 = new Table();
    Label loginLabel = new Label(
        "Login:",
        UiStyle.getLabelStyle(this.game.fontManager, 0, 24)
    );
    table2.add(loginLabel).padRight(4).right();
    TextField loginField = new TextField(
        "Werryx",
        UiStyle.getTextFieldStyle(this.game.fontManager, 0, 24)
    );
    table2.add(loginField).padLeft(4).width(220);
    table2.row();
    Label passwordLabel = new Label(
        "Password:",
        UiStyle.getLabelStyle(this.game.fontManager, 0, 24)
    );
    table2.add(passwordLabel).padRight(4).right();
    TextField passwordField = new TextField(
        "123456",
        UiStyle.getTextFieldStyle(this.game.fontManager, 0, 24)
    );
    passwordField.setPasswordMode(true);
    passwordField.setPasswordCharacter('•');
    table2.add(passwordField).padLeft(4).width(220);
    table2.pack();

    Table table3 = new Table();
    TextButton registerButton = new TextButton(
        "Register",
        UiStyle.getTextButtonStyle(this.game.fontManager, 0, 24)
    );
    registerButton.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        LoginScreen.this.game.logger.fine("Register button pressed");

        byte[] hashedPassword;
        byte[] textPasswordBytes = passwordField.getText().getBytes(StandardCharsets.UTF_8);
        byte[] loginBytes = loginField.getText().getBytes(StandardCharsets.UTF_8);
        byte[] passwordBytes = ByteBuffer.allocate(
                textPasswordBytes.length + loginBytes.length + LoginScreen.PEPPER.length)
            .put(textPasswordBytes).put(loginBytes).put(LoginScreen.PEPPER).array();

        try {
          hashedPassword = MessageDigest.getInstance("SHA3-256").digest(passwordBytes);
        } catch (NoSuchAlgorithmException e) {
          LoginScreen.this.game.logException(e);
          return;
        }

        int loginLength = loginBytes.length;
        ByteBuffer buffer = ByteBuffer.allocate(2 + 1 + loginLength + hashedPassword.length);
        buffer.putShort((short) 0);
        buffer.put((byte) loginLength);
        buffer.put(loginBytes);
        buffer.put(hashedPassword);
        LoginScreen.this.game.client.send(buffer);
      }
    });
    table3.add(registerButton).width(150).padRight(4);
    TextButton logInButton = new TextButton(
        "Log in",
        UiStyle.getTextButtonStyle(this.game.fontManager, 0, 24)
    );
    logInButton.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        LoginScreen.this.game.logger.fine("Log in button pressed");

        byte[] hashedPassword;
        byte[] textPasswordBytes = passwordField.getText().getBytes(StandardCharsets.UTF_8);
        byte[] loginBytes = loginField.getText().getBytes(StandardCharsets.UTF_8);
        byte[] passwordBytes = ByteBuffer.allocate(
                textPasswordBytes.length + loginBytes.length + LoginScreen.PEPPER.length)
            .put(textPasswordBytes).put(loginBytes).put(LoginScreen.PEPPER).array();

        try {
          hashedPassword = MessageDigest.getInstance("SHA3-256").digest(passwordBytes);
        } catch (NoSuchAlgorithmException e) {
          LoginScreen.this.game.logException(e);
          return;
        }

        int loginLength = loginBytes.length;
        ByteBuffer buffer = ByteBuffer.allocate(2 + 1 + loginLength + hashedPassword.length);
        buffer.putShort((short) 1);
        buffer.put((byte) loginLength);
        buffer.put(loginBytes);
        buffer.put(hashedPassword);
        LoginScreen.this.game.client.send(buffer);
      }
    });
    table3.add(logInButton).width(150).padLeft(4);
    table3.pack();

    Table panelTable = new Table();
    Label titleLabel = new Label(
        "Log in to account or create new",
        UiStyle.getLabelStyle(this.game.fontManager, 1, 30)
    );
    panelTable.add(titleLabel).padBottom(8);
    panelTable.row();
    panelTable.add(table2).padTop(4).padBottom(8);
    panelTable.row();
    panelTable.add(table3);
    panelTable.pack();

    Table table = new Table();
    table.add(panelTable).width(panelTable.getWidth() + 40).height(panelTable.getHeight() + 30);
    table.setFillParent(true);
    table.pack();

    table.setBackground(this.colorToDrawable(new Color(0x121212ff)));

    NinePatch panelNinePatch = new NinePatch(
        ResourceLoader.loadTexture(Gdx.files.internal("ui" + File.separator + "panel_bg.png")), 14,
        14, 14, 14);
    panelTable.setBackground(new NinePatchDrawable(panelNinePatch));

    this.game.stage.addActor(table);
  }

  @Override
  public void onMessage(int code, ByteBuffer message) {
    if (code == 0 || code == 6) {
      this.changeScreen(new ContactsScreen(this.game));
    }
  }

  @Override
  public void onDisconnect() {
    this.game.logger.fine("Disconnected from server");
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
