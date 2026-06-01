package com.freightos.fms.application.blquicksearch.port.in;

import com.freightos.fms.application.blquicksearch.command.BlQuickSearchCommand;
import com.freightos.fms.application.blquicksearch.projection.BlQuickSearchSummary;

import java.util.List;

public interface BlQuickSearchUseCase {

    List<BlQuickSearchSummary> quickSearch(BlQuickSearchCommand cmd);
}
