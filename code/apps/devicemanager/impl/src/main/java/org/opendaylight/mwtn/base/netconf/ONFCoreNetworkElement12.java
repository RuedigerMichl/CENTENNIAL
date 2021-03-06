/*
* Copyright (c) 2017 highstreet technologies GmbH and others. All rights reserved.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v1.0 which accompanies this distribution,
* and is available at http://www.eclipse.org/legal/epl-v10.html
*/

package org.opendaylight.mwtn.base.netconf;

import com.google.common.base.Optional;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.MountPoint;
import org.opendaylight.controller.md.sal.binding.api.NotificationService;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.mwtn.base.internalTypes.InternalDateAndTime;
import org.opendaylight.mwtn.base.internalTypes.InternalSeverity;
import org.opendaylight.mwtn.devicemanager.impl.database.service.HtDatabaseEventsService;
import org.opendaylight.mwtn.devicemanager.impl.listener.MicrowaveEventListener12;
import org.opendaylight.mwtn.devicemanager.impl.xml.ProblemNotificationXml;
import org.opendaylight.mwtn.devicemanager.impl.xml.WebSocketServiceClient;
import org.opendaylight.mwtn.ecompConnector.impl.EventProviderClient;
import org.opendaylight.mwtn.performancemanager.impl.database.types.EsHistoricalPerformance15Minutes;
import org.opendaylight.mwtn.performancemanager.impl.database.types.EsHistoricalPerformance24Hours;
import org.opendaylight.yang.gen.v1.uri.onf.microwavemodel.networkelement.currentproblemlist.rev161120.GenericCurrentProblemType;
import org.opendaylight.yang.gen.v1.uri.onf.microwavemodel.networkelement.currentproblemlist.rev161120.NetworkElementCurrentProblems;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.NetworkElement;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.UniversalId;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.extension.g.Extension;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.logical.termination.point.g.Lp;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.network.element.Ltp;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.g._874._1.model.rev170320.GranularityPeriodType;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.g._874._1.model.rev170320.OtnHistoryDataG;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.AirInterfaceCurrentProblemTypeG;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.AirInterfaceDiversityCurrentProblemTypeG;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.AirInterfaceHistoricalPerformanceTypeG;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.ContainerCurrentProblemTypeG;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.ContainerHistoricalPerformanceTypeG;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.MwAirInterfaceDiversityPac;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.MwAirInterfaceDiversityPacKey;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.MwAirInterfacePac;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.MwAirInterfacePacKey;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.MwEthernetContainerPac;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.MwEthernetContainerPacKey;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.MwHybridMwStructurePac;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.MwHybridMwStructurePacKey;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.MwPureEthernetStructurePac;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.MwPureEthernetStructurePacKey;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.MwTdmContainerPac;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.MwTdmContainerPacKey;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.StructureCurrentProblemTypeG;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.mw.air._interface.diversity.pac.AirInterfaceDiversityCurrentProblems;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.mw.air._interface.pac.AirInterfaceConfiguration;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.mw.air._interface.pac.AirInterfaceCurrentProblems;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.mw.air._interface.pac.AirInterfaceHistoricalPerformances;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.mw.ethernet.container.pac.EthernetContainerCurrentProblems;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.mw.ethernet.container.pac.EthernetContainerHistoricalPerformances;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.mw.hybrid.mw.structure.pac.HybridMwStructureCurrentProblems;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.mw.pure.ethernet.structure.pac.PureEthernetStructureCurrentProblems;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.mw.tdm.container.pac.TdmContainerCurrentProblems;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.onf.ethernet.conditional.packages.rev170402.EthernetPac;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Get information over NETCONF device according to ONF Coremodel. Read networkelement and conditional packages.
 *
 * Get conditional packages from Networkelement
 * Possible interfaces are:
 *   MWPS, LTP(MWPS-TTP), MWAirInterfacePac, MicrowaveModel-ObjectClasses-AirInterface
 *   ETH-CTP,LTP(Client), MW_EthernetContainer_Pac
 *   MWS, LTP(MWS-CTP-xD), MWAirInterfaceDiversityPac, MicrowaveModel-ObjectClasses-AirInterfaceDiversity
 *   MWS, LTP(MWS-TTP), ,MicrowaveModel-ObjectClasses-HybridMwStructure
 *   MWS, LTP(MWS-TTP), ,MicrowaveModel-ObjectClasses-PureEthernetStructure
 *
 * @author herbert
 *
 */
public class ONFCoreNetworkElement12 extends ONFCoreNetworkElementBase {

    private static final Logger LOG = LoggerFactory.getLogger(ONFCoreNetworkElement12.class);

    private static final List<Extension> EMPTYLTPEXTENSIONLIST = new ArrayList<>();
    //private static final List<Ltp> EMPTYLTPLIST = new ArrayList<>();

    private static final InstanceIdentifier<NetworkElement> NETWORKELEMENT_IID = InstanceIdentifier
            .builder(NetworkElement.class)
            .build();

    /*-----------------------------------------------------------------------------
     * Class members
     */

    // Non specific part. Used by all functions.
    /** interfaceList is used by PM task and should be synchonized */
    private final @Nonnull List<Lp> interfaceList = Collections.synchronizedList(new CopyOnWriteArrayList<>());
    private final @Nonnull MicrowaveEventListener12 microwaveEventListener;
    private @Nullable NetworkElement optionalNe = null;

    // Performance monitoring specific part
    /** Lock for the PM access specific elements that could be null */
    private final @Nonnull Object pmLock = new Object();
    private @Nullable Iterator<Lp> interfaceListIterator = null;
    /** Actual pmLp used during iteration over interfaces */
    private @Nullable Lp pmLp = null;

    // Device monitoring specific part
    /** Lock for the DM access specific elements that could be null */
    private final @Nonnull Object dmLock = new Object();
    /** Interface used for device monitoring (dm). If not null it contains the interface that is used for monitoring calls */
    private @Nullable InstanceIdentifier<AirInterfaceCurrentProblems> dmAirIfCurrentProblemsIID = null;

    /*-----------------------------------------------------------------------------
     * Construction
     */

    /**
     * Constructor
     * @param mountPointNodeName as String
     * @param capabilities of the specific network element
     * @param netconfNodeDataBroker for the network element specific data
     * @param webSocketService to forward event notifications
     * @param databaseService to access the database
     * @param ecompProvider to forward problem / change notifications
     */
    private ONFCoreNetworkElement12(String mountPointNodeName, Capabilities capabilities,
            DataBroker netconfNodeDataBroker, WebSocketServiceClient webSocketService,
            HtDatabaseEventsService databaseService, EventProviderClient ecompProvider ) {

        super(mountPointNodeName, netconfNodeDataBroker, capabilities );

        //Create MicrowaveService here
        this.microwaveEventListener = new MicrowaveEventListener12(mountPointNodeName, webSocketService, databaseService, ecompProvider);

        LOG.info("Create NE instance {}", NetworkElement.QNAME.getLocalName());
    }

    /**
     * Check capabilites are matching the this specific implementation and create network element representation if so.
     * @param mountPointNodeName as String
     * @param capabilities of the specific network element
     * @param netconfNodeDataBroker for the network element specific data
     * @param webSocketService to forward event notifications
     * @param databaseService to access the database
     * @param ecompProvider to forward problem / change notifications
     * @return created Object if conditions are OK or null if not.
     */
    public static @Nullable ONFCoreNetworkElement12 build(String mountPointNodeName, Capabilities capabilities,
            DataBroker netconfNodeDataBroker, WebSocketServiceClient webSocketService,
            HtDatabaseEventsService databaseService, EventProviderClient ecompProvider ) {
        return checkType(capabilities) ? new ONFCoreNetworkElement12(mountPointNodeName, capabilities, netconfNodeDataBroker, webSocketService, databaseService, ecompProvider ) : null;
    }

    /*-----------------------------------------------------------------------------
     * Functions
     */

    private static boolean checkType(Capabilities capabilities) {
        return capabilities.isSupportingNamespace(NetworkElement.QNAME);
    }

    /**
     * Check connection by requesting NetworkElement object
     * @see org.opendaylight.mwtn.base.netconf.ONFCoreNetworkElementRepresentation#checkAndConnectionToMediatorIsOk()
     */
    @Override
    public boolean checkAndConnectionToMediatorIsOk() {

        //Read to the config data store
        AtomicBoolean noErrorIndicator = new AtomicBoolean();
        AtomicReference<String> status = new AtomicReference<>();

        GenericTransactionUtils.readDataOptionalWithStatus(netconfNodeDataBroker, LogicalDatastoreType.OPERATIONAL, NETWORKELEMENT_IID, noErrorIndicator, status);
        LOG.debug("Status noErrorIndicator: {} statusTxt:{}",noErrorIndicator.get(), status.get());
        return noErrorIndicator.get();
    }

    /**
     * Check connection only possible if AirInterface (MWTN) exists. Request
     * @see org.opendaylight.mwtn.base.netconf.ONFCoreNetworkElementRepresentation#checkAndConnectionToNeIsOk()
     */
    @Override
    public boolean checkAndConnectionToNeIsOk() {
        synchronized (dmLock) {
            if (dmAirIfCurrentProblemsIID != null) {
                //Read to the config data store
                AtomicBoolean noErrorIndicator = new AtomicBoolean();
                AtomicReference<String> status = new AtomicReference<>();

                GenericTransactionUtils.readDataOptionalWithStatus(netconfNodeDataBroker, LogicalDatastoreType.OPERATIONAL, dmAirIfCurrentProblemsIID, noErrorIndicator, status);
                LOG.debug("Status noErrorIndicator: {} statusTxt:{}",noErrorIndicator.get(), status.get());
                return noErrorIndicator.get();
            }
        }
        return true;
    }

    /*-----------------------------------------------------------------------------
     * Problem/Fault related functions
     */

    /**
     * Read during startup all relevant structure and status parameters from device
     */
    @Override
    public void initialReadFromNetworkElement() {
        //optionalNe.getLtp().get(0).getLp();
        LOG.debug("Get info about {}", mountPointNodeName);

        int problems = microwaveEventListener.removeAllCurrentProblemsOfNode();
        LOG.debug("Removed all {} problems from database at registration",problems);

        //Step 2.1: access data broker within this mount point
        LOG.debug("DBRead start");

        //Step 2.2: read ne from data store
        optionalNe = readNetworkElement();
        if (optionalNe == null) {
            LOG.debug("NE Name: no NE");

        } else {

            LOG.debug("NE Name: {}", optionalNe.getName().toString());
            synchronized(pmLock) {
                interfaceList.addAll( getLtpList(optionalNe) );
            }

            //Step 2.3: read the existing faults and add to DB
            List<ProblemNotificationXml> resultList = new ArrayList<>();
            int idxStart; //Start index for debug messages
            UniversalId uuid;

            synchronized(pmLock) {
                for (Lp ltp : interfaceList) {

                    idxStart = resultList.size();
                    uuid = ltp.getUuid();
                    Class<?> lpClass = getLpExtension(ltp);

                    ONFLayerProtocolName lpName = ONFLayerProtocolName.valueOf(ltp.getLayerProtocolName());
                    switch(lpName) {
                        case MWAirInterface:
                            readTheFaultsOfMwAirInterfacePac(uuid, resultList);
                            synchronized (dmLock) {
                                if (dmAirIfCurrentProblemsIID == null) {
                                    dmAirIfCurrentProblemsIID = getMWAirInterfacePacIId(uuid);
                                }
                            }
                            break;

                        case EthernetContainer12:
                            readTheFaultsOfMwEthernetContainerPac(uuid, resultList);
                            break;

                        case TDMContainer:
                            readTheFaultsOfMwTdmContainerPac(uuid, resultList);
                            break;

                        case Structure:
                            if (lpClass == MwHybridMwStructurePac.class) {
                                readTheFaultsOfMwHybridMwStructurePac(uuid, resultList);

                            } else if (lpClass == MwAirInterfaceDiversityPac.class) {
                                readTheFaultsOfMwAirInterfaceDiversityPac(uuid, resultList);

                            } else if (lpClass == MwPureEthernetStructurePac.class) {
                                readTheFaultsOfMwPureEthernetStructurePac(uuid, resultList);

                            } else {
                                LOG.warn("Unassigned lp model {} class {}", lpName, lpClass);
                            }
                            break;

                        case Ethernet:
                            //No alarms supported
                            break;
                        case EthernetContainer10:
                        default:
                            LOG.warn("Unassigned or not expected lp in model {}", lpName);
                    }

                    debugResultList(uuid.getValue(), resultList, idxStart);

                }
            }

            //Step 2.4: Read other problems from Mountpoint
            if (isNetworkElementCurrentProblemsSupporting) {
                idxStart = resultList.size();
                readNetworkElementCurrentProblems(resultList);
                debugResultList("CurrentProblems", resultList, idxStart);
            }
            microwaveEventListener.initCurrentProblem(resultList);

            LOG.info("Found info at {} for device {} number of problems: {}", mountPointNodeName, getUuId(), resultList.size());
        }
    }


    /**
     * LOG the newly added problems of the interface pac
     * @param idxStart
     * @param uuid
     * @param resultList
     */
    private void debugResultList( String uuid, List<ProblemNotificationXml> resultList, int idxStart  ) {
        if (LOG.isDebugEnabled()) {
            StringBuffer sb = new StringBuffer();
            int idx = 0;
            for (int t = idxStart; t < resultList.size(); t++) {
                sb.append(idx++);
                sb.append(":{");
                sb.append(resultList.get(t));
                sb.append('}');
            }
            LOG.debug("Found problems {} {}",uuid, sb.toString());
        }
    }

    /**
     * Get uuid of Optional NE.
     * @return Uuid or EMPTY String if optionNE is not available
     */
    private String getUuId() {
        String uuid = EMPTY;

        try {
            uuid = optionalNe != null ? optionalNe.getUuid() != null ? optionalNe.getUuid().getValue() : EMPTY : EMPTY;
        } catch (NullPointerException  e) {
            //Unfortunately throws null pointer if not definied
        }
        return uuid;
    }

    /**
     * Read the NetworkElement part from database.
     * @return Optional with NetworkElement or empty
     */
    @Nullable
    private NetworkElement readNetworkElement() {
        //Step 2.2: construct data and the relative iid
        // The schema path to identify an instance is
        // <i>CoreModel-CoreNetworkModule-ObjectClasses/NetworkElement</i>
        /*
        InstanceIdentifier<NetworkElement> networkElementIID = InstanceIdentifier
                .builder(NetworkElement.class)
                .build();
        */
        //Read to the config data store
        return GenericTransactionUtils.readData(netconfNodeDataBroker, LogicalDatastoreType.OPERATIONAL, NETWORKELEMENT_IID);
    }

    /**
     * Get from LayProtocolExtensions the related generated ONF Interface PAC class
     * which represents it.
     *
     * @param ltp logical termination point
     * @return Class of InterfacePac
     */
    @Nullable
    private static Class<?> getLpExtension(@Nullable Lp ltp) {

        String capability = EMPTY;
        String revision = EMPTY;
        String conditionalPackage = EMPTY;
        Class<?> res = null;

        if (ltp != null) {
            for (Extension e : getExtensionList(ltp)) {
                if (e.getValueName().contentEquals("capability")) {
                    capability = e.getValue();
                    int idx = capability.indexOf("?");
                    if (idx != -1) {
                        capability = capability.substring(0, idx);
                    }
                }
                if (e.getValueName().contentEquals("revision")) {
                    revision = e.getValue();
                }
                if (e.getValueName().contentEquals("conditional-package")) {
                    conditionalPackage = e.getValue();
                }
            }
        }
        //QName qName = org.opendaylight.yangtools.yang.common.QName.create("urn:onf:params:xml:ns:yang:microwave-model",
        //                                     "2017-03-24", "mw-air-interface-pac").intern();
        LOG.info("LpExtension capability={} revision={} conditionalPackage={}", capability, revision, conditionalPackage);
        if (!capability.isEmpty() && !revision.isEmpty() && !conditionalPackage.isEmpty()) {
            try {
                QName qName = QName.create(capability, revision, conditionalPackage);

                if (qName.equals(MwAirInterfacePac.QNAME)) {
                    res = MwAirInterfacePac.class;
                } else if (qName.equals(MwAirInterfaceDiversityPac.QNAME)) {
                    res = MwAirInterfaceDiversityPac.class;
                } else if (qName.equals(MwPureEthernetStructurePac.QNAME)) {
                    res = MwPureEthernetStructurePac.class;
                } else if (qName.equals(MwHybridMwStructurePac.QNAME)) {
                    res = MwHybridMwStructurePac.class;
                } else if (qName.equals(MwEthernetContainerPac.QNAME)) {
                    res = MwEthernetContainerPac.class;
                } else if (qName.equals(MwTdmContainerPac.QNAME)) {
                    res = MwTdmContainerPac.class;
                } else if (qName.equals(EthernetPac.QNAME)) {
                    res = MwTdmContainerPac.class;
                }
                LOG.info("Found QName {} mapped to {}", String.valueOf(qName), String.valueOf(res));
            } catch (IllegalArgumentException e) {
                LOG.debug("Can not create QName from ({}{{}{{}): {}",capability, revision, conditionalPackage, e.getMessage());
            }
        }
        return res;
    }


    /**
     * Read element from class that could be not available
     * @param ltp layer termination point
     * @return List with extension parameters or empty list
     */
    @Nonnull
    private static List<Extension> getExtensionList(@Nullable Lp ltp) {
        if (ltp != null && ltp.getExtension() != null) {
               return ltp.getExtension();
        } else {
            return EMPTYLTPEXTENSIONLIST;
        }
    }

    /**
     * Get List of UUIDs for conditional packages from Networkelement<br>
     * Possible interfaces are:<br>
     *   MWPS, LTP(MWPS-TTP), MWAirInterfacePac, MicrowaveModel-ObjectClasses-AirInterface<br>
     *   ETH-CTP,LTP(Client), MW_EthernetContainer_Pac<br>
     *   MWS, LTP(MWS-CTP-xD), MWAirInterfaceDiversityPac, MicrowaveModel-ObjectClasses-AirInterfaceDiversity<br>
     *   MWS, LTP(MWS-TTP), ,MicrowaveModel-ObjectClasses-HybridMwStructure<br>
     *   MWS, LTP(MWS-TTP), ,MicrowaveModel-ObjectClasses-PureEthernetStructure<br>
     * @param ne Networkelement
     * @return Id List, never null.
     */
    private List<Lp> getLtpList( NetworkElement ne ) {

        List<Lp> res = Collections.synchronizedList(new ArrayList<Lp>());

        if (ne != null) {
            List<Ltp> ltpRefList = ne.getLtp();
            if (ltpRefList == null) {
                LOG.debug("DBRead NE-Interfaces: null");
            } else {
                for (Ltp ltRefListE : ltpRefList ) {
                    List<Lp> lpList = ltRefListE.getLp();
                    if (lpList == null) {
                        LOG.debug("DBRead NE-Interfaces Reference List: null");
                    } else {
                        for (Lp ltp : lpList) {
                            ////LayerProtocolName layerProtocolName = lpListE.getLayerProtocolName();
                            //UniversalId uuId = lpListE.getUuid();
                            res.add(ltp);
                        }
                    }
                }
            }
        } else {
            LOG.debug("DBRead NE: null");
        }

        //---- Debug
        if (LOG.isDebugEnabled()) {
            StringBuffer strBuf = new StringBuffer();
            for (Lp ltp : res) {
                if (strBuf.length() > 0) {
                    strBuf.append(", ");
                }
                strBuf.append(ltp.getLayerProtocolName().getValue());
                strBuf.append(':');
                strBuf.append(ltp.getUuid().getValue());
            }
            LOG.debug("DBRead NE-Interfaces: {}", strBuf.toString());
        }
        //---- Debug end

        return res;
    }


    /*-----------------------------------------------------------------------------
     * Performance related data
     */


    /**
     * PM MwAirInterfacePac
     * @param lp
     * @return
     */
    private List<ExtendedAirInterfaceHistoricalPerformanceType12> readTheHistoricalPerformanceDataOfMwAirInterfacePac(Lp lp) {

        String uuId = lp.getUuid().getValue();

        List<ExtendedAirInterfaceHistoricalPerformanceType12> resultList = new ArrayList<>();
        LOG.debug("DBRead Get {} MWAirInterfacePac: {}", mountPointNodeName, uuId);
        //----
        UniversalId mwAirInterfacePacuuId = new UniversalId(uuId);
        //Step 2.1: construct data and the relative iid
        InstanceIdentifier<AirInterfaceConfiguration> mwAirInterfaceConfigurationIID = InstanceIdentifier
                .builder(MwAirInterfacePac.class, new MwAirInterfacePacKey(mwAirInterfacePacuuId))
                .child(AirInterfaceConfiguration.class)
                .build();
        AirInterfaceConfiguration airConfiguration = GenericTransactionUtils.readData(netconfNodeDataBroker, LogicalDatastoreType.OPERATIONAL, mwAirInterfaceConfigurationIID);

        //Step 2.2: construct data and the relative iid
        InstanceIdentifier<AirInterfaceHistoricalPerformances> mwAirInterfaceHistoricalPerformanceIID = InstanceIdentifier
                .builder(MwAirInterfacePac.class, new MwAirInterfacePacKey(mwAirInterfacePacuuId))
                .child(AirInterfaceHistoricalPerformances.class)
                .build();

        //Step 2.3: read to the config data store
        AirInterfaceHistoricalPerformances airHistoricalPerformanceData = GenericTransactionUtils.readData(netconfNodeDataBroker, LogicalDatastoreType.OPERATIONAL, mwAirInterfaceHistoricalPerformanceIID);

        if (airHistoricalPerformanceData == null) {
            LOG.debug("DBRead MWAirInterfacePac Id {} no HistoricalPerformances", mwAirInterfacePacuuId);
        } else {
            //org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170320.air._interface.historical.performances.g.HistoricalPerformanceDataList
            List<org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.air._interface.historical.performances.g.HistoricalPerformanceDataList> airHistPMList = airHistoricalPerformanceData.getHistoricalPerformanceDataList();
            LOG.debug("DBRead MWAirInterfacePac Id {} Records intermediate: {}",mwAirInterfacePacuuId, airHistPMList.size());
            if (airHistPMList != null) {
                for ( AirInterfaceHistoricalPerformanceTypeG pmRecord : airHistoricalPerformanceData.getHistoricalPerformanceDataList()) {
                    resultList.add(new ExtendedAirInterfaceHistoricalPerformanceType12(pmRecord, airConfiguration));
                }
            }
        }
        LOG.debug("DBRead MWAirInterfacePac Id {} Records result: {}",mwAirInterfacePacuuId, resultList.size());
        return resultList;
    }

    private List<ContainerHistoricalPerformanceTypeG> readTheHistoricalPerformanceDataOfEthernetContainer(Lp lp) {

        final String myName = "MWEthernetContainerPac";
        String uuId = lp.getUuid().getValue();

        List<ContainerHistoricalPerformanceTypeG> resultList = new ArrayList<>();
        LOG.debug("DBRead Get {} : {}", mountPointNodeName, myName,uuId);
        //----
        UniversalId ethContainerPacuuId = new UniversalId(uuId);
        //Step 2.2: construct data and the relative iid
        InstanceIdentifier<EthernetContainerHistoricalPerformances> ethContainerIID = InstanceIdentifier
                .builder(MwEthernetContainerPac.class, new MwEthernetContainerPacKey(ethContainerPacuuId))
                .child(EthernetContainerHistoricalPerformances.class)
                .build();

        //Step 2.3: read to the config data store
        EthernetContainerHistoricalPerformances ethContainerHistoricalPerformanceData = GenericTransactionUtils.readData(netconfNodeDataBroker, LogicalDatastoreType.OPERATIONAL, ethContainerIID);

        if (ethContainerHistoricalPerformanceData == null) {
            LOG.debug("DBRead {} Id {} no HistoricalPerformances", myName, ethContainerPacuuId);
        } else {
            //import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170320.ethernet.container.historical.performances.g.HistoricalPerformanceDataList
            List<org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.ethernet.container.historical.performances.g.HistoricalPerformanceDataList> airHistPMList = ethContainerHistoricalPerformanceData.getHistoricalPerformanceDataList();
            LOG.debug("DBRead {} Id {} Records intermediate: {}", myName, ethContainerPacuuId, airHistPMList.size());
            if (airHistPMList != null) {
                for ( ContainerHistoricalPerformanceTypeG pmRecord : airHistPMList) {
                    resultList.add(pmRecord);
                }
            }
        }
        LOG.debug("DBRead {} Id {} Records result: {}", myName, ethContainerPacuuId, resultList.size());
        return resultList;
    }

    @Nonnull
    private List<? extends OtnHistoryDataG> readTheHistoricalPerformanceData(Lp lp) {
        ONFLayerProtocolName lpName = ONFLayerProtocolName.valueOf(lp.getLayerProtocolName());

        switch( lpName ) {
            case MWAirInterface:
                return readTheHistoricalPerformanceDataOfMwAirInterfacePac(lp);

            case EthernetContainer12:
                return readTheHistoricalPerformanceDataOfEthernetContainer(lp);

            case EthernetContainer10:
            case EthernetPhysical:
            case Ethernet:
            case TDMContainer:
            case Structure:
            case Unknown:
                LOG.debug("Do not read HistoricalPM data for {} {}", lpName, lp.getUuid().getValue());
                break;
        }
        return new ArrayList<>();
    }


    @Override
    public AllPm getHistoricalPM() {

        synchronized(pmLock) {
            if (pmLp != null) {
                LOG.debug("Enter query PM");
                AllPm allPm = new AllPm();
                Lp lp = pmLp;

                List<? extends OtnHistoryDataG> resultList = readTheHistoricalPerformanceData(lp);
                LOG.debug("Got records: {}", resultList.size());
                //org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.g._874._1.model.rev170320.GranularityPeriodType
                GranularityPeriodType granularityPeriod;
                for (OtnHistoryDataG perf : resultList) {

                    granularityPeriod = perf.getGranularityPeriod();
                    if (granularityPeriod == null) {
                        granularityPeriod = GranularityPeriodType.Unknown;
                    }

                    switch(granularityPeriod) {
                        case Period15Min: {
                            EsHistoricalPerformance15Minutes pm = new EsHistoricalPerformance15Minutes(mountPointNodeName, lp)
                                    .setHistoricalRecord15Minutes(perf);
                            allPm.add(pm);
                            }
                            break;

                        case Period24Hours: {
                            EsHistoricalPerformance24Hours pm = new EsHistoricalPerformance24Hours(mountPointNodeName, lp)
                                    .setHistoricalRecord24Hours(perf);
                            LOG.debug("Write 24h write to DB");
                            allPm.add(pm);
                            }
                            break;

                        default:
                            LOG.warn("Unknown granularity {}",perf.getGranularityPeriod());
                            break;

                    }
                }
                LOG.debug("Deliver normalized records: {}", allPm.size());
                return allPm;
            } else {
                LOG.debug("Deliver empty, no LTP");
                return AllPm.EMPTY;
            }
        }


    }

    @Override
    public void resetPMIterator() {
        synchronized(pmLock) {
            interfaceListIterator = interfaceList.iterator();
        }
    }

    @Override
    public boolean hasNext() {
        synchronized(pmLock) {
            return interfaceListIterator.hasNext();
        }
    }

    @Override
    public void next() {
        synchronized(pmLock) {
            pmLp = interfaceListIterator.next();
        }
    }

    @Override
    public String pmStatusToString() {
        synchronized(pmLock) {
            return pmLp == null ? "no interface" : pmLp.getLayerProtocolName().getValue()+":"+pmLp.getUuid().getValue();
        }
    }


    /*------------------------------------------------------------
     * private function to access database
     */

    /*-----------------------------------------------------------------------------
     * Reading problems for the networkElement
     */

    private List<ProblemNotificationXml> readNetworkElementCurrentProblems(List<ProblemNotificationXml> resultList) {

        LOG.info("DBRead Get {} NetworkElementCurrentProblems", mountPointNodeName);

        InstanceIdentifier<NetworkElementCurrentProblems> networkElementCurrentProblemsIID = InstanceIdentifier
                .builder(NetworkElementCurrentProblems.class)
                .build();

        //Step 2.3: read to the config data store
        NetworkElementCurrentProblems problems;
        try {
            problems = GenericTransactionUtils.readData(netconfNodeDataBroker, LogicalDatastoreType.OPERATIONAL, networkElementCurrentProblemsIID);
            if (problems == null) {
                LOG.debug("DBRead NetworkElementCurrentProblems no CurrentProblemsList");
            } if (problems.getCurrentProblemList() == null) {
                LOG.debug("DBRead NetworkElementCurrentProblems empty CurrentProblemsList");
            } else {
                for(GenericCurrentProblemType problem : problems.getCurrentProblemList()) {
                   resultList.add(new ProblemNotificationXml(mountPointNodeName, problem.getObjectIdRef(),
                           problem.getProblemName(), InternalSeverity.valueOf(problem.getProblemSeverity()),
                           problem.getSequenceNumber().toString(), InternalDateAndTime.valueOf(problem.getTimeStamp())));
                }
            }
        } catch (Exception e) {
            LOG.warn( "DBRead {} NetworkElementCurrentProblems not supported. Message '{}' ", mountPointNodeName, e.getMessage() );
        }
        return resultList;
    }

    /*-----------------------------------------------------------------------------
     * Reading problems for specific interface pacs
     */

    /**
     * Generate ID
     * @param interfacePacUuid for airinterface
     * @return AirInterfaceCurrentProblemsIID
     */
    private InstanceIdentifier<AirInterfaceCurrentProblems> getMWAirInterfacePacIId(UniversalId interfacePacUuid) {
        InstanceIdentifier<AirInterfaceCurrentProblems> mwAirInterfaceIID = InstanceIdentifier
                .builder(MwAirInterfacePac.class, new MwAirInterfacePacKey(interfacePacUuid))
                .child(AirInterfaceCurrentProblems.class)
                .build();
        return mwAirInterfaceIID;
    }

    /**
     * Read problems of specific interfaces
     *
     * @param uuId Universal Id String of the interface
     * @return number of alarms
     */

    private List<ProblemNotificationXml> readTheFaultsOfMwAirInterfacePac(UniversalId interfacePacUuid, List<ProblemNotificationXml> resultList) {

        final Class<MwAirInterfacePac> clazzPac = MwAirInterfacePac.class;
        //final Class<MwAirInterfacePacKey> clazzPacKey = MwAirInterfacePacKey.class;
        //final Class<AirInterfaceCurrentProblems> clazzProblems = AirInterfaceCurrentProblems.class;
        //final Class<AirInterfaceCurrentProblemTypeG> clazzProblem = AirInterfaceCurrentProblemTypeG.class;

        LOG.info("DBRead Get problems for class {} from mountpoint {} for uuid {}", clazzPac.getSimpleName(),mountPointNodeName, interfacePacUuid.getValue());

        //Step 2.2: construct data and the relative iid
        InstanceIdentifier<AirInterfaceCurrentProblems> mwAirInterfaceIID = InstanceIdentifier
                .builder(MwAirInterfacePac.class, new MwAirInterfacePacKey(interfacePacUuid))
                .child(AirInterfaceCurrentProblems.class)
                .build();

        //Step 2.3: read to the config data store
        AirInterfaceCurrentProblems problems = GenericTransactionUtils.readData(netconfNodeDataBroker, LogicalDatastoreType.OPERATIONAL, mwAirInterfaceIID);

        if (problems == null) {
            LOG.debug("DBRead AirProblems Id {} no CurrentProblemsList", interfacePacUuid);
        } if (problems.getCurrentProblemList() == null) {
            LOG.debug("DBRead Id {} empty CurrentProblemsList", interfacePacUuid);
        } else {
            for ( AirInterfaceCurrentProblemTypeG problem : problems.getCurrentProblemList()) {
                resultList.add(new ProblemNotificationXml(mountPointNodeName, interfacePacUuid.getValue(),
                        problem.getProblemName(), InternalSeverity.valueOf(problem.getProblemSeverity()),
                        problem.getSequenceNumber().toString(), InternalDateAndTime.valueOf(problem.getTimeStamp())));

            }
        }
        return resultList;
    }

    /**
     * Read problems of specific interfaces
    *
     * @param uuId Universal index of Interfacepac
     * @return number of alarms
     */

    private List<ProblemNotificationXml> readTheFaultsOfMwEthernetContainerPac(UniversalId interfacePacUuid, List<ProblemNotificationXml> resultList) {

        final Class<MwEthernetContainerPac> clazzPac = MwEthernetContainerPac.class;
        //final Class<MwEthernetContainerPacKey> clazzPacKey = MwEthernetContainerPacKey.class;
        //final Class<EthernetContainerCurrentProblems> clazzProblems = EthernetContainerCurrentProblems.class;
        //final Class<ContainerCurrentProblemTypeG> clazzProblem = ContainerCurrentProblemTypeG.class;

        LOG.info("DBRead Get problems for class {} from mountpoint {} for uuid {}", clazzPac.getSimpleName(),mountPointNodeName, interfacePacUuid.getValue());

        InstanceIdentifier<EthernetContainerCurrentProblems> mwEthInterfaceIID = InstanceIdentifier
                .builder(MwEthernetContainerPac.class, new MwEthernetContainerPacKey(interfacePacUuid))
                .child(EthernetContainerCurrentProblems.class)
                .build();

        EthernetContainerCurrentProblems problems = GenericTransactionUtils.readData(netconfNodeDataBroker, LogicalDatastoreType.OPERATIONAL, mwEthInterfaceIID);
        if (problems == null) {
            LOG.debug("DBRead Id {} no CurrentProblemsList", interfacePacUuid);
        } if (problems.getCurrentProblemList() == null) {
            LOG.debug("DBRead Id {} empty CurrentProblemsList", interfacePacUuid);
        } else {
            for ( ContainerCurrentProblemTypeG problem : problems.getCurrentProblemList()) {
                resultList.add(new ProblemNotificationXml(mountPointNodeName, interfacePacUuid.getValue(),
                        problem.getProblemName(), InternalSeverity.valueOf(problem.getProblemSeverity()),
                        problem.getSequenceNumber().toString(), InternalDateAndTime.valueOf(problem.getTimeStamp())));
            }
        }
        return resultList;
    }

    /**
     * Read problems of specific interfaces
     *
     * @param uuId Universal index of Interfacepac
     * @return number of alarms
     */
    private List<ProblemNotificationXml> readTheFaultsOfMwAirInterfaceDiversityPac(UniversalId interfacePacUuid, List<ProblemNotificationXml> resultList) {

        final Class<MwAirInterfaceDiversityPac> clazzPac = MwAirInterfaceDiversityPac.class;
        //final Class<MwAirInterfaceDiversityPacKey> clazzPacKey = MwAirInterfaceDiversityPacKey.class;
        final Class<AirInterfaceDiversityCurrentProblems> clazzProblems = AirInterfaceDiversityCurrentProblems.class;
        //final Class<AirInterfaceDiversityCurrentProblemTypeG> clazzProblem = AirInterfaceDiversityCurrentProblemTypeG.class;

        LOG.info("DBRead Get problems for class {} from mountpoint {} for uuid {}", clazzPac.getSimpleName(),mountPointNodeName, interfacePacUuid.getValue());

        InstanceIdentifier<AirInterfaceDiversityCurrentProblems> mwEthInterfaceIID = InstanceIdentifier
                .builder(clazzPac, new MwAirInterfaceDiversityPacKey(interfacePacUuid))
                .child(clazzProblems)
                .build();

        AirInterfaceDiversityCurrentProblems problems = GenericTransactionUtils.readData(netconfNodeDataBroker, LogicalDatastoreType.OPERATIONAL, mwEthInterfaceIID);
        if (problems == null) {
            LOG.debug("DBRead Id {} no CurrentProblemsList", interfacePacUuid);
        } if (problems.getCurrentProblemList() == null) {
            LOG.debug("DBRead Id {} empty CurrentProblemsList", interfacePacUuid);
        } else {
            for ( AirInterfaceDiversityCurrentProblemTypeG problem : problems.getCurrentProblemList()) {
                resultList.add(new ProblemNotificationXml(mountPointNodeName, interfacePacUuid.getValue(),
                        problem.getProblemName(), InternalSeverity.valueOf(problem.getProblemSeverity()),
                        problem.getSequenceNumber().toString(), InternalDateAndTime.valueOf(problem.getTimeStamp())));
            }
        }
        return resultList;
    }

    /**
     * Read problems of specific interfaces
     *
     * @param uuId Universal index of Interfacepac
     * @return number of alarms
     */
    private List<ProblemNotificationXml> readTheFaultsOfMwPureEthernetStructurePac(UniversalId interfacePacUuid, List<ProblemNotificationXml> resultList) {

        final Class<MwPureEthernetStructurePac> clazzPac = MwPureEthernetStructurePac.class;
        //final Class<MwPureEthernetStructurePacKey> clazzPacKey = MwPureEthernetStructurePacKey.class;
        final Class<PureEthernetStructureCurrentProblems> clazzProblems = PureEthernetStructureCurrentProblems.class;
        //final Class<StructureCurrentProblemTypeG> clazzProblem = StructureCurrentProblemTypeG.class;

        LOG.info("DBRead Get problems for class {} from mountpoint {} for uuid {}", clazzPac.getSimpleName(),mountPointNodeName, interfacePacUuid.getValue());

        InstanceIdentifier<PureEthernetStructureCurrentProblems> mwEthInterfaceIID = InstanceIdentifier
                .builder(clazzPac, new MwPureEthernetStructurePacKey(interfacePacUuid))
                .child(clazzProblems)
                .build();

        PureEthernetStructureCurrentProblems problems = GenericTransactionUtils.readData(netconfNodeDataBroker, LogicalDatastoreType.OPERATIONAL, mwEthInterfaceIID);
        if (problems == null) {
            LOG.debug("DBRead Id {} no CurrentProblemsList", interfacePacUuid);
        } if (problems.getCurrentProblemList() == null) {
            LOG.debug("DBRead Id {} empty CurrentProblemsList", interfacePacUuid);
        } else {
            for ( StructureCurrentProblemTypeG problem : problems.getCurrentProblemList()) {
                resultList.add(new ProblemNotificationXml(mountPointNodeName, interfacePacUuid.getValue(),
                        problem.getProblemName(), InternalSeverity.valueOf(problem.getProblemSeverity()),
                        problem.getSequenceNumber().toString(), InternalDateAndTime.valueOf(problem.getTimeStamp())));
            }
        }
        return resultList;
    }

    /**
     * Read problems of specific interfaces
     *
     * @param uuId Universal index of Interfacepac
     * @return number of alarms
     */
    private List<ProblemNotificationXml> readTheFaultsOfMwHybridMwStructurePac(UniversalId interfacePacUuid, List<ProblemNotificationXml> resultList) {

        final Class<MwHybridMwStructurePac> clazzPac = MwHybridMwStructurePac.class;
        //final Class<MwHybridMwStructurePacKey> clazzPacKey = MwHybridMwStructurePacKey.class;
        final Class<HybridMwStructureCurrentProblems> clazzProblems = HybridMwStructureCurrentProblems.class;
        //final Class<HybridMwStructureCurrentProblemsG> clazzProblem = HybridMwStructureCurrentProblemsG.class;

        LOG.info("DBRead Get problems for class {} from mountpoint {} for uuid {}", clazzPac.getSimpleName(),mountPointNodeName, interfacePacUuid.getValue());

        InstanceIdentifier<HybridMwStructureCurrentProblems> mwEthInterfaceIID = InstanceIdentifier
                .builder(clazzPac, new MwHybridMwStructurePacKey(interfacePacUuid))
                .child(clazzProblems)
                .build();

        HybridMwStructureCurrentProblems problems = GenericTransactionUtils.readData(netconfNodeDataBroker, LogicalDatastoreType.OPERATIONAL, mwEthInterfaceIID);
        if (problems == null) {
            LOG.debug("DBRead Id {} no CurrentProblemsList", interfacePacUuid);
        } if (problems.getCurrentProblemList() == null) {
            LOG.debug("DBRead Id {} empty CurrentProblemsList", interfacePacUuid);
        } else {
            for ( StructureCurrentProblemTypeG problem : problems.getCurrentProblemList()) {
                resultList.add(new ProblemNotificationXml(mountPointNodeName, interfacePacUuid.getValue(),
                        problem.getProblemName(), InternalSeverity.valueOf(problem.getProblemSeverity()),
                        problem.getSequenceNumber().toString(), InternalDateAndTime.valueOf(problem.getTimeStamp())));

            }
        }
        return resultList;
    }

    /**
     * Read problems of specific interfaces
     *
     * @param uuId Universal index of Interfacepac
     * @return number of alarms
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private List<ProblemNotificationXml> readTheFaultsOfMwTdmContainerPac(UniversalId interfacePacUuid, List<ProblemNotificationXml> resultList) {

        final Class<MwTdmContainerPac> clazzPac = MwTdmContainerPac.class;
        final Class<MwTdmContainerPacKey> clazzPacKey = MwTdmContainerPacKey.class;
        final Class<TdmContainerCurrentProblems> clazzProblems = TdmContainerCurrentProblems.class;
        //final Class<ContainerCurrentProblemTypeG> clazzProblem = ContainerCurrentProblemTypeG.class;

        LOG.info("DBRead Get problems for class {} from mountpoint {} for uuid {}", clazzPac.getSimpleName(),mountPointNodeName, interfacePacUuid.getValue());

        try {
            //-- Specific part 1
            Constructor<MwTdmContainerPacKey> cons = clazzPacKey.getConstructor(UniversalId.class); //Avoid new()
            InstanceIdentifier<TdmContainerCurrentProblems> mwEthInterfaceIID = InstanceIdentifier
                    .builder(clazzPac, cons.newInstance(interfacePacUuid))
                    .child(clazzProblems)
                    .build();

            //-- Specific part 2
            TdmContainerCurrentProblems problems = GenericTransactionUtils.readData(netconfNodeDataBroker, LogicalDatastoreType.OPERATIONAL, mwEthInterfaceIID);
            if (problems == null) {
                LOG.debug("DBRead Id {} no CurrentProblemsList", interfacePacUuid);
            } if (problems.getCurrentProblemList() == null) {
                LOG.debug("DBRead Id {} empty CurrentProblemsList", interfacePacUuid);
            } else {
                //-- Specific part 3
                for ( ContainerCurrentProblemTypeG problem : problems.getCurrentProblemList()) {
                    resultList.add(new ProblemNotificationXml(mountPointNodeName, interfacePacUuid.getValue(),
                            problem.getProblemName(), InternalSeverity.valueOf(problem.getProblemSeverity()),
                            problem.getSequenceNumber().toString(), InternalDateAndTime.valueOf(problem.getTimeStamp())));

                }
            }
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException e) {
            LOG.error("(..something..) failed", e);
        }
        return resultList;
    }


    /**
     * Remove all entries from list
     */
    @Override
    public int removeAllCurrentProblemsOfNode() {
        return microwaveEventListener.removeAllCurrentProblemsOfNode();
    }

    /**
     * Register the listener
     */
    @Override
    public void doRegisterMicrowaveEventListener(MountPoint mountPoint) {
        final Optional<NotificationService> optionalNotificationService = mountPoint.getService(NotificationService.class);
        final NotificationService notificationService = optionalNotificationService.get();
        //notificationService.registerNotificationListener(microwaveEventListener);
        notificationService.registerNotificationListener(microwaveEventListener);
    }


}
