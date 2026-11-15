package ua.kpi.pw03.data.remote

import ua.kpi.pw03.data.model.NetScenario

object ScenarioStore {
    @Volatile
    var current: NetScenario = NetScenario.SUCCESS
}
