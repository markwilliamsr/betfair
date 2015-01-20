package com.betfair.aping.entities;

import com.betfair.aping.enums.ExecutionReportErrorCode;
import com.betfair.aping.enums.ExecutionReportStatus;

import java.util.List;

/**
 * Created by markwilliams on 1/19/15.
 */
public class CancelExecutionReport {
    String customerRef;
    ExecutionReportStatus status;
    ExecutionReportErrorCode errorCode;
    String marketId;
    List<CancelExecutionReport> instructionReports;
}
