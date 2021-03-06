/*
* Copyright (c) 2017 highstreet technologies GmbH and others. All rights reserved.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v1.0 which accompanies this distribution,
* and is available at http://www.eclipse.org/legal/epl-v10.html
*/

package org.opendaylight.mwtn.devicemanager.impl.listener;

import org.opendaylight.mwtn.base.internalTypes.InternalDateAndTime;
import org.opendaylight.mwtn.base.internalTypes.InternalSeverity;
import org.opendaylight.mwtn.base.netconf.NetconfTimeStamp;
import org.opendaylight.mwtn.devicemanager.impl.database.service.HtDatabaseEventsService;
import org.opendaylight.mwtn.devicemanager.impl.xml.ObjectCreationNotificationXml;
import org.opendaylight.mwtn.devicemanager.impl.xml.ObjectDeletionNotificationXml;
import org.opendaylight.mwtn.devicemanager.impl.xml.ProblemNotificationXml;
import org.opendaylight.mwtn.devicemanager.impl.xml.WebSocketServiceClient;
import org.opendaylight.mwtn.ecompConnector.impl.EventProviderClient;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.ProblemNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible class for documenting changes in the ODL itself.
 * The occurence of such an event is documented in the database and to clients.
 * Specific example here is the registration or deregistration of a netconf device.
 * This service has an own eventcounter to apply to the ONF Coremodel netconf behaviour.
 *
 * Important: Websocket notificatin must be the last action.
 *
 * @author herbert
 */

public class ODLEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(ODLEventListener.class);

    //private static final String ATTRIBUTENAME = "netconf-session";

    private final String ownKeyName;

    private final WebSocketServiceClient webSocketService;
    //private final WebsocketmanagerService websocketmanagerService;
    //private final XmlMapper xmlMapper;
    private final HtDatabaseEventsService databaseService;
    private final EventProviderClient ecompProvider;

    private int eventNumber;



    /*---------------------------------------------------------------
     * Construct
     */

    /**
     * Create a Service to document events to clients and within a database
     * @param ownKeyName The name of this service, that is used in the database as identification key.
     * @param webSocketService service to direct messages to clients
     * @param databaseService service to write to the database
     * @param ecompProvider to deliver problems to
     */
    public ODLEventListener(String ownKeyName, WebSocketServiceClient webSocketService,
            HtDatabaseEventsService databaseService, EventProviderClient ecompProvider) {
        super();

        this.ownKeyName = ownKeyName;

        this.webSocketService = webSocketService;
        //this.xmlMapper = xmlMapper;
        //this.websocketmanagerService = websocketmanagerService;

        this.databaseService = databaseService;
        this.ecompProvider = ecompProvider;

        this.eventNumber = 0;

    }

    /*---------------------------------------------------------------
     * Functions
     */

    /**
     * A registration of a mountpoint occured.
     * @param registrationName Name of the event that is used as key in the database.
     */

    public void registration(String registrationName) {

        /*
        ObjectCreationNotificationBuilder builder = new ObjectCreationNotificationBuilder();
        builder.setCounter(popEvntNumber());
        builder.setObjectIdRef(new UniversalId(registrationName));
        builder.setObjectType(ATTRIBUTENAME);
        builder.setTimeStamp(NetconfTimeStamp.getTimeStamp());
        ObjectCreationNotification notification = builder.build();
        */
        //ObjectCreationNotificationXml cNotificationXml = new ObjectCreationNotificationXml(ownKeyName, notification);
        ObjectCreationNotificationXml cNotificationXml = new ObjectCreationNotificationXml(
                ownKeyName,    popEvntNumberAsString(), InternalDateAndTime.valueOf(NetconfTimeStamp.getTimeStamp()), registrationName);

        // Write first to prevent missing entries
        databaseService.writeEventLog(cNotificationXml);

        /*try {
            WebsocketEventInputBuilder wsBuilder = new WebsocketEventInputBuilder();
            wsBuilder.setNodeName(ownKeyName);
            wsBuilder.setEventType(ObjectCreationNotification.class.getSimpleName());
            wsBuilder.setXmlEvent(xmlMapper.getXmlString(cNotificationXml));
            websocketmanagerService.websocketEvent(wsBuilder.build());
        } catch (Exception e) {
            LOG.warn("Can not send websocket event registration for mountpoint {} {}", registrationName, e.toString());
        }*/
        webSocketService.sendViaWebsockets(registrationName, cNotificationXml);

    }

    /**
     * A deregistration of a mountpoint occured.
     * @param registrationName Name of the event that is used as key in the database.
     */

    public void deRegistration(String registrationName) {
        /*
        ObjectDeletionNotificationBuilder builder = new ObjectDeletionNotificationBuilder();
        builder.setCounter(popEvntNumber());
        builder.setObjectIdRef(new UniversalId(registrationName));
        builder.setTimeStamp(NetconfTimeStamp.getTimeStamp());
        ObjectDeletionNotification notification = builder.build();
        */
        //ObjectDeletionNotificationXml dNotificationXml = new ObjectDeletionNotificationXml(ownKeyName, notification);
        ObjectDeletionNotificationXml dNotificationXml = new ObjectDeletionNotificationXml(ownKeyName,
                popEvntNumberAsString(),
                InternalDateAndTime.valueOf(NetconfTimeStamp.getTimeStamp()),
                registrationName
                );


        // Write first to prevent missing entries
        databaseService.writeEventLog(dNotificationXml);

        /*try {
            WebsocketEventInputBuilder wsBuilder = new WebsocketEventInputBuilder();
            wsBuilder.setNodeName(ownKeyName);
            wsBuilder.setEventType(ObjectDeletionNotification.class.getSimpleName());
            wsBuilder.setXmlEvent(xmlMapper.getXmlString(dNotificationXml));
            websocketmanagerService.websocketEvent(wsBuilder.build());
        } catch (Exception e) {
            LOG.warn("Can not send websocket event deregistration for mountpoint {} {}", registrationName, e.toString());
        }*/
        webSocketService.sendViaWebsockets(registrationName, dNotificationXml);

    }

    /**
     * At a mountpoint a problem situation is indicated
     * @param registrationName indicating object within SDN controller, normally the mountpointName
     * @param problemName that changed
     * @param problemSeverity of the problem according to NETCONF/YANG
     */

    public void onProblemNotification(String registrationName, String problemName, InternalSeverity problemSeverity) {
        LOG.debug("Got event of type :: {}", ProblemNotification.class.getSimpleName());
        //notification

        ProblemNotificationXml notificationXml = new ProblemNotificationXml(ownKeyName,
                registrationName, problemName, problemSeverity,
                //popEvntNumberAsString(), InternalDateAndTime.TESTPATTERN );
                popEvntNumberAsString(), InternalDateAndTime.valueOf(NetconfTimeStamp.getTimeStamp()));

        databaseService.writeFaultLog(notificationXml);
        databaseService.updateFaultCurrent(notificationXml);

        ecompProvider.sendProblemNotification(ownKeyName, notificationXml);

        webSocketService.sendViaWebsockets(registrationName, notificationXml);
   }

    /**
     * Called on exit to remove everything for a node from the current list.
     * @param nodeName to remove all problems for
     * @return Number of deleted objects
     */
    public int removeAllCurrentProblemsOfNode(String nodeName) {
        return databaseService.clearFaultsCurrentOfNodeWithObjectId(ownKeyName, nodeName);
    }


    /*---------------------------------------------------------------
     * Private
     */

    private String popEvntNumberAsString() {
        return String.valueOf(popEvntNumber());
    }

    private int popEvntNumber() {
        return eventNumber++;
    }
    /*
    private <T extends MwtNotificationBase & GetEventType> void sendViaWebsockets(String registrationName, T notificationXml) {
        LOG.info("Send websocket event {} for mountpoint {}", notificationXml.getClass().getSimpleName(), registrationName);

        try {
            WebsocketEventInputBuilder wsBuilder = new WebsocketEventInputBuilder();
            wsBuilder.setNodeName(ownKeyName);
            wsBuilder.setEventType(notificationXml.getEventType());
            wsBuilder.setXmlEvent(xmlMapper.getXmlString(notificationXml));
            Future<RpcResult<WebsocketEventOutput>> result = websocketmanagerService.websocketEvent(wsBuilder.build());
            LOG.info("Send websocket result: {}", result.get().getResult().getResponse());
        } catch (Exception e) {
            LOG.warn("Can not send websocket event {} for mountpoint {} {}",
                    notificationXml.getClass().getSimpleName(), registrationName, e.toString());
        }
    }
    */
}
