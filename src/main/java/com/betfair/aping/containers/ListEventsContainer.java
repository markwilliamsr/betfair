package com.betfair.aping.containers;

import com.betfair.aping.entities.EventResult;

import java.util.List;

public class ListEventsContainer extends Container {

	private List<EventResult> result;

	public List<EventResult> getResult() {
		return result;
	}

	public void setResult(List<EventResult> result) {
		this.result = result;
	}

}
