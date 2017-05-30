package nl.everlutions.directionhearing;

/**
 * Created by jaapo on 30-5-2017.
 */

public interface IMessageHandler<MessageType> {
    void handle(MessageType message);
}
