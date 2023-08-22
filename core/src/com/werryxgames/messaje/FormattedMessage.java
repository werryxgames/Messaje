package com.werryxgames.messaje;

import com.badlogic.gdx.scenes.scene2d.ui.Table;

/**
 * Class, that represents message with, created for it, table.
 *
 * @since 1.0
 */
public class FormattedMessage {

  public Message message;
  public Table table;

  public FormattedMessage() {
  }

  public FormattedMessage(Message message, Table table) {
    this.message = message;
    this.table = table;
  }
}
