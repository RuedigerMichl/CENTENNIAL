/*
 * utils.h
 *
 *  Created on: Aug 12, 2016
 *      Author: compila
 */

#ifndef UTILS_H_
#define UTILS_H_

#include <stdio.h>
#include "status.h"
#include "val.h"
#include "obj.h"
#include "val_util.h"
#include "agt_util.h"
#include "procdefs.h"

#define NOP

#define YUMA_ASSERT(condition, action, fmt, ...) 	\
({													\
	if ((condition))								\
	{												\
		fprintf(stderr, "WT_3.PoC: %s at %s line %d: "fmt"\n", __FILE__,__FUNCTION__,__LINE__, ##__VA_ARGS__); \
		action;										\
	} 												\
})

#define LTP_MWPS_PREFIX "LTP-MWPS-"
#define LP_MWPS_PREFIX "LP-MWPS-"
#define LTP_MWS_PREFIX "LTP-MWS-"
#define LP_MWS_PREFIX "LP-MWS-"
#define LTP_ETH_CTP_PREFIX "LTP-ETH-CTP-"
#define LP_ETH_CTP_PREFIX "LP-ETH-CTP-"

#define MWPS_PREFIX "MWPS"
#define MWS_PREFIX "MWS"
#define ETH_CTP_PREFIX "ETH-CTP"

#define MAX_NUMBER_OF_AIR_INTERFACE_PAC 50
#define MAX_NUMBER_OF_PROBLEM_KIND_SEVERITY_ENTRIES 1000
#define MAX_NUMBER_OF_CHANNEL_PLAN_TYPE_ID_ENTRIES 100
#define MAX_NUMBER_OF_TRANSMISSION_MODE_ID_ENTRIES 100
#define MAX_NUMBER_OF_AIR_INTERFACE_PAC_AIR_INTERFACE_CURRENT_PROBLEM_LIST_ENTRIES 1000
#define MAX_NUMBER_OF_AIR_INTERFACE_PAC_AIR_INTERFACE_HISTORICAL_PERFORMANCE_LIST_ENTRIES 1000
#define MAX_NUMBER_OF_AIR_INTERFACE_PAC_AIR_INTERFACE_CURRENT_PERFORMANCE_LIST_ENTRIES 50
#define MAX_NUMBER_OF_CO_CHANNEL_GROUP_LIST_ENTRIES 50
#define MAX_NUMBER_OF_CO_CHANNEL_GROUP_AIR_INTERFACE_LIST_ENTRIES 50

#define MAX_NUMBER_OF_LTP_REF_ENTRIES 100
#define MAX_NUMBER_OF_SERVER_CLIENT_REF_ENTRIES 1000

#define MAX_NUMBER_OF_PURE_ETHERNET_STRUCTURE_ENTRIES 50
#define MAX_NUMBER_OF_PURE_ETHERNET_STRUCTURE_CURRENT_PROBLEM_LIST_ENTRIES 1000


#define MAX_NUMBER_OF_ETHERNET_CONTAINER_PAC 50
#define MAX_NUMBER_OF_ETHERNET_CONTAINER_PAC_ETHERNET_CONTAINER_PROBLEM_LIST_ENTRIES 1000
#define MAX_NUMBER_OF_ETHERNET_CONTAINER_PAC_ETHERNET_CONTAINER_SEGMENT_LIST_ENTRIES 10
#define MAX_NUMBER_OF_ETHERNET_CONTAINER_PAC_ETHERNET_CONTAINER_CURRENT_PROBLEM_LIST_ENTRIES 1000
#define MAX_NUMBER_OF_ETHERNET_CONTAINER_PAC_ETHERNET_CONTAINER_HISTORICAL_PERFORMANCE_LIST_ENTRIES 1000
#define MAX_NUMBER_OF_ETHERNET_CONTAINER_PAC_ETHERNET_CONTAINER_CURRENT_PERFORMANCE_LIST_ENTRIES 50

#define ALARM_RAISED 1
#define ALARM_CLEARED 0

status_t create_root_element_for_module(const char *module_name, const char *revision, const char *element_name, val_value_t** created_element_val);

status_t create_and_init_child_element(const char *modname, const char *objname, val_value_t *parent_val, val_value_t **child_val, const char *valuestr, const char* moduleName, bool isRuntime);

status_t create_and_init_siblings(obj_template_t *curr_obj,	val_value_t *parent_val, const char* moduleName, bool isRuntime);

status_t add_virtual_leaf(val_value_t *parentVal, const char *elementName, getcb_fn_t callbackFunction);

#endif /* UTILS_H_ */
