package org.swdc.fx.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swdc.fx.FXApplication;
import org.swdc.fx.event.AssociationEvent;
import org.swdc.fx.net.data.MainParameter;

import java.util.Arrays;

public class MainParamHandler extends ExternalHandler<MainParameter> {

    protected Logger logger = LoggerFactory.getLogger(MainParamHandler.class);

    @Override
    public void accept(FXApplication attachment, MainParameter message) {
        logger.warn("external messsage : " + Arrays.toString(message.getArgs()));
        attachment.emit(new AssociationEvent(message, null));
    }

}
