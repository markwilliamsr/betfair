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
    List<CancelInstructionReport> instructionReports;

    public String getCustomerRef() {
        return customerRef;
    }

    public void setCustomerRef(String customerRef) {
        this.customerRef = customerRef;
    }

    public ExecutionReportStatus getStatus() {
        return status;
    }

    public void setStatus(ExecutionReportStatus status) {
        this.status = status;
    }

    public ExecutionReportErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ExecutionReportErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public String getMarketId() {
        return marketId;
    }

    public void setMarketId(String marketId) {
        this.marketId = marketId;
    }

    public List<CancelInstructionReport> getInstructionReports() {
        return instructionReports;
    }

    public void setInstructionReports(List<CancelInstructionReport> instructionReports) {
        this.instructionReports = instructionReports;
    }
}
