#!/bin/bash
# Copyright (c) 2017, Bernard Butler (Waterford Institute of Technology, Ireland), Project: SOLAS placement in Amadeus SA, where SOLAS (Project ID: 612480) is funded by the European Commision FP7 MC-IAPP-Industry-Academia Partnerships and Pathways scheme.
# All rights reserved.
# 
# Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
# 
#  -  Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
#  -  Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
#  -  Neither the name of WATERFORD INSTITUTE OF TECHNOLOGY nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
# 
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
db=$1
base=$2
[ ! -d output/$base ] && mkdir -vp output/$base
sqlite3 $db << END_TEXT
.headers on
.mode list
.output output/$base/typeStatus_summary.txt
SELECT Type, TransactionStatus, COUNT(*) AS Count FROM fum GROUP BY Type, TransactionStatus;
.output output/$base/dcsKO.txt
SELECT * FROM fum WHERE Type="dcsDocument" AND TransactionStatus="KO";
.output output/$base/dcs_summary.txt
SELECT TransactionStatus, TransactionCode, TransactionSubCode, COUNT(*) AS Count FROM fum WHERE Type="dcsDocument" GROUP BY TransactionStatus, TransactionCode, TransactionSubCode;
.output output/$base/dcsKOerror_summary.txt
SELECT Errors, COUNT(*) AS Count FROM fum WHERE Type="dcsDocument" AND TransactionStatus="KO" GROUP BY Errors;
.output output/$base/dcsKOwarning_summary.txt
SELECT Warnings, COUNT(*) AS Count FROM fum WHERE Type="dcsDocument" AND TransactionStatus="KO" GROUP BY Warnings;
.output output/$base/etkKO.txt
SELECT * FROM fum WHERE Type="etkDocument" AND TransactionStatus="KO";
.output output/$base/etk_summary.txt
SELECT TransactionStatus, TransactionCode, TransactionSubCode, COUNT(*) AS Count FROM fum WHERE Type="etkDocument" GROUP BY TransactionStatus, TransactionCode, TransactionSubCode;
.output output/$base/etkKOerror_summary.txt
SELECT Errors, COUNT(*) AS Count FROM fum WHERE Type="etkDocument" AND TransactionStatus="KO" GROUP BY Errors;
.output output/$base/etkKOwarning_summary.txt
SELECT Warnings, COUNT(*) AS Count FROM fum WHERE Type="etkDocument" AND TransactionStatus="KO" GROUP BY Warnings;
END_TEXT

