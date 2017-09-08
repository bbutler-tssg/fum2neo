# Uploading data from FUM extracts to neo4j.

## Current status

At present, FUM is queried manually using the `elasticsearch` webform. Because of the memory required
to render the results in his browser, the results are limited to 5000 records for each query that is run.

The critical fields are

1. `Type` represents different applications (etk, dcs and couchbase AppEvents are available).

2. `Prefix`. In principle, this can be parsed to extract the `DcxID` and `TreeId`, which together enable a given FUM
record to be linked to a CorMel single `Usegment`. However, sometimes the `Prefix` just contains the `DcxID`.

3. `TransactionStatus`. Generally this can be either `OK` (success) or `KO` (failure). Evidence suggests it is
not populated for couchbase AppEvents, nor does it appear to be populated for etk AppEvents. In the latter, it is
possible that the presence of `Error` or `Warning` fields suggest that a `TransactionStatus` of "KO" could be implied,
but this is not guaranteed. 

4. `TransactionCode` and `TransactionSubCode`. Some document types (such as `etkDocument`) use these - they may provide
extra context.

5. `Error` and `Warning` each take the form of a list of items, with each item comprising a code with explanatory text.
These might provide supporting context when the `TransactionStatus` is `KO`.

The data is currently supplied in JSON format, and software has been written to:

1. Parse this data and extract the fields of interest

2. Process the data and output the relevant fields in CSV files.

3. *Optionally*, update existing `U`  and `T` nodes in the neo4j database, assuming they were uploaded previously from CorMel
files. *Alternatively*, generate and save (in an Avro file) AppEvent data that can be used to filter and label `U` and `T` records
as they are being uploaded from CorMel files in future.

4. Load exported data from csv files into a sqlite database

5. Run SQL queries to analyse the data in the sqlite database and try to interpret its significance.

In principle,
each extracted AppEvent record from FUM should map to a single `CorMel` `T` node, assuming the data was taken from the same time
period.

## Processing pipeline

For convenience the processing pipeline has been collected into a single script and can be run as follows (where the
names of the FUM data extracts are provided as a combined command line argument):

    script/upload.sh "Sample_FUM_DataSets_allWithTrxnStatus Sample_FUM_DataSets_unfilteredByTrxnStatus"

## Results

Type|TransactionStatus|Count
--- | --- | ---
couchbaseDocument||1314
dcsDocument|CONTRL - Query Decoding Error|1
dcsDocument|KO|197
dcsDocument|OK|6475
etkDocument|KO|83
etkDocument|OK|1930

As can be seen above, `couchbaseDocument` does not use `TransactionStatus` and so is ignored for the rest of this
analysis.

Clearly, there is a one-off special `TransactionStatus` for one of the `dcsDocument` records. This might require further
analysis.

TransactionStatus|TransactionCode|TransactionSubCode|Count
--- | --- | --- | ---
CONTRL - Query Decoding Error|||1
KO|||197
OK|||6475

As can be seen in the Table above, DCS *does not* provide values of `TransactionCode` or `TransactionSubCode`, in contrast
to ETK, which *does*:

TransactionStatus|TransactionCode|TransactionSubCode|Count
--- | --- | --- | ---
KO|130||1
KO|131||57
KO|131|CND|2
KO|131|CSC|2
KO|131|FUL|3
KO|131|PAX|1
KO|139|ETI|3
KO|142||7
KO|791||4
KO|UPD||3
OK|10||4
OK|107||13
OK|130||29
OK|130|ETI|13
OK|131||1482
OK|131|707|1
OK|131|CNS|4
OK|131|CSC|10
OK|131|ETI|39
OK|131|EXT|1
OK|131|FUL|5
OK|133||3
OK|134||4
OK|134|ETI|1
OK|135||1
OK|137|EXT|4
OK|139|ETI|4
OK|142||141
OK|17||11
OK|733||1
OK|734||1
OK|751||40
OK|791||78
OK|792||7
OK|793||7
OK|794||2
OK|796||3
OK|798||1
OK|799||1
OK|BYPASS||6
OK|CCD||1
OK|EFS||2
OK|ELI||2
OK|ELI|130|1
OK|UPD||7

The next type of analysis was to look at the Errors and Warning associated with `TransactionStatus` of `KO`.

Generally, the text associated with each error/warning suggests that these are *application* rather than *system* problems.
However some are ambiguous: `["11 - UNABLE TO PROCESS"]` could also be a system problem.

Errors|Count
--- | ---
["11 - UNABLE TO PROCESS"]|4
["11020 - INVALID IDENTIFIER"]|1
["13254 - Specific seat requested is not available."]|1
["13279 - Specific seat requested not available - restricted."]|1
["1386 - SEGMENT NOT FOUND"]|1
["14000 - Invalid flight details."]|8
["14035 - Flight acceptance finalised"]|1
["14123 - Itinerary already exists"]|1
["14158 - No agreement for through check-in"]|1
["14255 - Customer is not eligible to transfer"]|1
["14427 - Invalid search criteria supplied"]|2
["14454 - No comment records exist for specified carrier"]|2
["14474 - Missing nationality";"14729 - Customer passport number required";"14730 - Customers given name required.";"14731 - Customers surname required.";"14733 - Expiry date of passport required.";"14734 - Passport country of issue required";"14799 - Other regulatory document identifier required";"14800 - Other regulatory document number required";"14801 - Expiry date of other regulatory document required"]|1
["14474 - Missing nationality";"14802 - Emergency contact name required";"14804 - Emergency contact telephone no country code required"]|1
["14474 - Missing nationality";"19225 - iAPP status - check not performed. Customer(s) regulatory data is incomplete. Add missing data and retry."]|5
["14474 - Missing nationality";"19329 - ADC status - check not performed. Customer(s) regulatory data is incomplete. Add missing data and retry."]|2
["14474 - Missing nationality"]|10
["14475 - Gender required";"14476 - Date of birth required";"14729 - Customer passport number required";"14730 - Customers given name required.";"14731 - Customers surname required.";"14733 - Expiry date of passport required.";"14734 - Passport country of issue required";"14800 - Other regulatory document number required";"14801 - Expiry date of other regulatory document required"]|1
["14476 - Date of birth required";"14729 - Customer passport number required";"14730 - Customers given name required.";"14731 - Customers surname required.";"14733 - Expiry date of passport required.";"14734 - Passport country of issue required";"14800 - Other regulatory document number required";"14801 - Expiry date of other regulatory document required"]|1
["14476 - Date of birth required";"14729 - Customer passport number required";"14730 - Customers given name required.";"14731 - Customers surname required.";"14733 - Expiry date of passport required.";"14734 - Passport country of issue required"]|1
["14519 - Customer product could not be retrieved"]|1
["14540 - No bag could be retrieved"]|12
["14729 - Customer passport number required";"14730 - Customers given name required.";"14731 - Customers surname required.";"14732 - Customers country of residence required.";"14733 - Expiry date of passport required.";"14734 - Passport country of issue required";"14799 - Other regulatory document identifier required";"14800 - Other regulatory document number required";"14801 - Expiry date of other regulatory document required"]|4
["14729 - Customer passport number required";"14730 - Customers given name required.";"14731 - Customers surname required.";"14732 - Customers country of residence required.";"14733 - Expiry date of passport required.";"14734 - Passport country of issue required";"14800 - Other regulatory document number required";"14801 - Expiry date of other regulatory document required"]|1
["14729 - Customer passport number required";"14730 - Customers given name required.";"14733 - Expiry date of passport required.";"14734 - Passport country of issue required"]|1
["14729 - Customer passport number required";"14733 - Expiry date of passport required.";"14734 - Passport country of issue required";"14799 - Other regulatory document identifier required";"14802 - Emergency contact name required";"14803 - Emergency contact telephone number required";"14804 - Emergency contact telephone no country code required"]|1
["14729 - Customer passport number required";"14733 - Expiry date of passport required.";"14734 - Passport country of issue required";"14801 - Expiry date of other regulatory document required"]|2
["14729 - Customer passport number required";"14799 - Other regulatory document identifier required";"14800 - Other regulatory document number required";"14802 - Emergency contact name required";"14803 - Emergency contact telephone number required";"14804 - Emergency contact telephone no country code required";"17717 - Collect regulatory information for Domestic flights."]|1
["14729 - Customer passport number required";"14799 - Other regulatory document identifier required";"14800 - Other regulatory document number required";"14802 - Emergency contact name required";"14803 - Emergency contact telephone number required";"14804 - Emergency contact telephone no country code required"]|1
["14729 - Customer passport number required";"14799 - Other regulatory document identifier required";"14838 - Passport/Other travel document already present - Entry not authorised."]|1
["14732 - Customers country of residence required.";"14802 - Emergency contact name required";"14803 - Emergency contact telephone number required";"14804 - Emergency contact telephone no country code required"]|1
["14732 - Customers country of residence required."]|1
["14740 - Destination address number and street name required";"14763 - Destination address city required";"14764 - Destination address state/province/county required";"14765 - Destination address zip code required";"17331 - Destination address country required"]|3
["14765 - Destination address zip code required"]|1
["14802 - Emergency contact name required";"14803 - Emergency contact telephone number required";"14804 - Emergency contact telephone no country code required"]|7
["14802 - Emergency contact name required";"14804 - Emergency contact telephone no country code required"]|2
["14838 - Passport/Other travel document already present - Entry not authorised."]|3
["14881 - No flight found matching search criteria"]|3
["15054 - Destination Airport Code Not Found"]|1
["15069 - Flight Leg Not Found"]|7
["15111 - No Staff Found"]|2
["17048 - No APIS data"]|1
["17275 - No excess baggage rates found"]|1
["17336 - Record in use - Re-enter"]|54
["17399 - Error decoding swiped data"]|4
["17523 - Unable to print boarding pass.";"18097 - Flight has departed"]|2
["17898 - Seat number 007C on boarding pass does not match current seat number 003A."]|1
["18052 - Not authorised - Company level"]|1
["18201 - FQTV number already present"]|2
["19181 - No records could be retrieved."]|1
["19204 - Entry not authorised (permission - CMT_CID_IDENTIFY_CUSTOMER_AND_PRODUCT_STD)."]|2
["19204 - Entry not authorised (permission - SEA_SEA_DISPLAY_SEAT_MAPS_FNE)."]|1
["19335 - Purpose of visit is required.";"19452 - Visa is required for UNITED STATES OF AMERICA."]|1
["19401 - Ticket number not found"]|10
["19913 - Item/data not found or data not existing in processing host"]|1
["20007 - Not Authorised WBT_CAP_QUERY_OR_RECEIVE_DEADLOAD_UPD_RAM_A TP YYZ"]|1
["26694 - No LMC Data exists"]|1
["27866 - Message from regulatory authority - ."]|1
["387 - SIMULTANEOUS CHANGES TO PNR - USE WRA/RT TO PRINT OR IGNORE"]|1

For ETK, there are fewer types of error, and most seem to relate to the application rather than the system; see below.
Perhaps error codes 427 and/or 913 relate to the system rather than just reflecting inconsistencies with the business logic.

Errors|Count
--- | ---
["107 - INVALID AIRLINE DESIGNATOR/VENDOR SUPPLIER"]|1
["118 - SYSTEM UNABLE TO PROCESS"]|1
["368 - NOT AUTHORISED"]|7
["395 - ALREADY TICKETED"]|1
["401 - TICKET NUMBER NOT FOUND"]|54
["427 - UNABLE TO PROCESS"]|2
["70D - INVALID SEARCH CRITERIA"]|8
["752 - REVALIDATION REQUEST DENIED"]|2
["75Z - ETKT RJT - NO INTERLINE BETWEEN CARRIERS"]|1
["913 - ITEM/DATA NOT FOUND OR DATA NOT EXISTING"]|6

# Usage in practice

Because uploading CorMel files sequentially takes so long (~14 hours for one
minute's worth (128 files) of transaction trees), it was decided to offer an
alternative to the "upload CorMel data, then update the records with a selected
(much smaller set) of AppEvents" approach.

The alternative is to output the AppEvents in a form that can be used to filter
incoming CorMel transaction trees (rooted in their U segments), depending on
whether the transaction tree includes at least one transaction with a matching
AppEvent record. Since the number of AppEvents collected per elasticsearch
query is limited to 5000, the number of uploaded CorMel transaction trees is
greatly reduced, as is the CorMel upload runtime (to less than 15 minutes), by
following this alternative "filter first" procedure.

The revised `upload.sh` now defaults to this `filterFirst` strategy, so it can
be called as follows:

    script/upload.sh "goodBadForSample20 FUM_DCS_NW_2017_08_29"

where the first of these AppEvent files is a manually edited file containing 2
AppEvent records. One has `TransactionStatus` "OK" and the other has
`TransactionStatus` "KO", with associated `Error`s and `Warning`s. The `DcxId`
and `TrxNb` fields have been edited to match equivalent transaction records in
the `sample20` set of transaction trees used when developing the `cormel2neo`
project.

The resulting `output/goodBadForSample20.gz.avro` and
`output/FUM_DCS_NW_2017_08_29.gz.avro` files can be used when uploading CorMel
data using the `cormel2neo` project.

The `FUM_DCS_NW_2017_08_29` AppEvents relate to a network outage that affected
traffic that tried to cross a particular link.  The elasticsearch query against
Sentinel selected just DCS events, to ensure the `TransactionStatus` field is
populated.  The corresponding CorMel files take the form
`par_U170829_093700_S170829_093500_D60_lgcap*.gz.avro`.

