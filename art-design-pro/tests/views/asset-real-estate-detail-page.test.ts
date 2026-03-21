import { beforeEach, describe, expect, it, vi } from "vitest"
import { reactive, ref } from "vue"
import { flushPromises, mount } from "@vue/test-utils"
import ElementPlus from "element-plus"
import AssetRealEstateDetailPage from "@/views/asset/real-estate/detail/index.vue"

const mockPush = vi.fn()
const routeState = reactive({
  params: { assetId: "20001" },
  meta: {},
  path: "/asset/real-estate/detail/20001",
  fullPath: "/asset/real-estate/detail/20001"
})

vi.mock("vue-router", async (importOriginal) => {
  const actual = await importOriginal<typeof import("vue-router")>()
  return {
    ...actual,
    useRouter: () => ({ push: mockPush }),
    useRoute: () => routeState
  }
})

vi.mock("@/utils/dict", () => {
  return {
    useDict: () => ({
      ast_asset_status: ref([]),
      ast_asset_source_type: ref([]),
      ast_asset_acquire_type: ref([])
    })
  }
})

vi.mock("@/store/modules/user", () => {
  return {
    useUserStore: () => ({
      permissions: ["asset:realEstate:query", "asset:realEstate:edit"]
    })
  }
})

vi.mock("@/api/asset/real-estate", () => {
  return {
    getRealEstateDetail: vi.fn().mockResolvedValue({
      data: {
        assetId: 20001,
        assetCode: "RE-2026-0001",
        assetName: "???????A?",
        assetStatus: "IN_USE",
        ownerDeptName: "????",
        useDeptName: "????",
        responsibleUserName: "??",
        locationName: "????????A?",
        ownershipCertNo: "?(2024)????????A0001?",
        landUseType: "????",
        buildingArea: 18650.5,
        originalValue: 12500000,
        lastInventoryDate: "2026-03-01",
        remark: "????????????"
      }
    }),
    getRealEstateLifecycle: vi.fn().mockResolvedValue({
      data: {
        ledger: { assetId: 20001 },
        handoverRecords: [
          {
            handoverOrderId: 1,
            handoverItemId: 11,
            handoverNo: "HO-2026-0001",
            handoverType: "TRANSFER",
            handoverDate: "2026-02-18",
            beforeStatus: "IN_USE",
            afterStatus: "IN_USE",
            toDeptName: "????",
            toUserName: "??",
            toLocationName: "????????A?"
          }
        ],
        inventoryRecords: [
          {
            taskId: 5,
            taskNo: "INV-2026-0007",
            taskName: "一季度不动产巡检",
            inventoryResult: "DAMAGED",
            followUpAction: "UPDATE_LEDGER",
            followUpBizId: 9001,
            processStatus: "PENDING",
            checkedBy: "资产管理员",
            checkedTime: "2026-02-20 09:30:00",
            resultDesc: "消防门闭合器损坏"
          },
          {
            taskId: 6,
            taskNo: "INV-2026-0008",
            taskName: "????????",
            inventoryResult: "LOCATION_DIFF",
            followUpAction: "UPDATE_LEDGER",
            processStatus: "PENDING",
            checkedBy: "?????",
            checkedTime: "2026-03-01 09:30:00",
            resultDesc: "房间实际使用人与台账不一致"
          }
        ],
        disposalRecords: [
          {
            disposalId: 9,
            disposalNo: "DIS-2026-0003",
            disposalType: "SCRAP",
            disposalStatus: "CONFIRMED",
            disposalDate: "2026-03-10",
            confirmedBy: "????",
            confirmedTime: "2026-03-10 14:20:00"
          }
        ],
        changeLogs: [
          {
            logId: 100,
            bizType: "LEDGER_CREATE",
            changeDesc: "????????",
            operateBy: "admin",
            operateTime: "2026-03-01 10:00:00",
            beforeStatus: "-",
            afterStatus: "IN_USE"
          },
          {
            logId: 101,
            bizType: "LEDGER_UPDATE",
            changeDesc: "???????????",
            operateBy: "asset-admin",
            operateTime: "2026-03-02 15:30:00",
            beforeStatus: "IN_USE",
            afterStatus: "IN_USE"
          }
        ]
      }
    })
  }
})

describe("AssetRealEstateDetailPage ?????", () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockPush.mockReset()
    routeState.params = { assetId: "20001" }
    routeState.meta = {}
    routeState.path = "/asset/real-estate/detail/20001"
    routeState.fullPath = "/asset/real-estate/detail/20001"
  })

  it("????????????", async () => {
    const wrapper = mount(AssetRealEstateDetailPage, {
      global: {
        plugins: [ElementPlus],
        stubs: { DictTag: true }
      }
    })

    await flushPromises()

    expect(wrapper.text()).toContain("????")
    expect(wrapper.text()).toContain("????")
    expect(wrapper.text()).toContain("????")
    expect(wrapper.text()).toContain("????")
    expect(wrapper.text()).toContain("????")
    expect(wrapper.text()).toContain("整改")
  })

  it("???????????????????", async () => {
    routeState.path = "/asset/real-estate/detail/20001/occupancy"
    routeState.fullPath = routeState.path

    const wrapper = mount(AssetRealEstateDetailPage, {
      global: {
        plugins: [ElementPlus],
        stubs: { DictTag: true }
      }
    })

    await flushPromises()

    expect(wrapper.text()).toContain("??????")
    expect(wrapper.text()).toContain("????")
    expect(wrapper.text()).toContain("HO-2026-0001")
  })

  it("???????????????????", async () => {
    routeState.path = "/asset/real-estate/detail/20001/inspection"
    routeState.fullPath = routeState.path

    const wrapper = mount(AssetRealEstateDetailPage, {
      global: {
        plugins: [ElementPlus],
        stubs: { DictTag: true }
      }
    })

    await flushPromises()

    expect(wrapper.text()).toContain("????")
    expect(wrapper.text()).toContain("????????")
    expect(wrapper.text()).toContain("????")
    expect(wrapper.text()).toContain("发起整改")

    const taskDetailButton = wrapper.get('[data-testid="inspection-task-link-6"]')
    await taskDetailButton.trigger("click")

    expect(mockPush).toHaveBeenCalledWith("/asset/real-estate/detail/20001/inspection-task/6")

    const rectificationCreateButton = wrapper.get('[data-testid="rectification-create-link-6"]')
    await rectificationCreateButton.trigger("click")

    expect(mockPush).toHaveBeenCalledWith({
      path: "/asset/real-estate/detail/20001/rectification/create",
      query: {
        taskId: "6"
      }
    })

    const rectificationEditButton = wrapper.get('[data-testid="rectification-edit-link-9001"]')
    await rectificationEditButton.trigger("click")

    expect(mockPush).toHaveBeenCalledWith("/asset/real-estate/detail/20001/rectification/edit/9001")
  })

  it("???????????????????????", async () => {
    routeState.path = "/asset/real-estate/detail/20001/rectification"
    routeState.fullPath = routeState.path

    const wrapper = mount(AssetRealEstateDetailPage, {
      global: {
        plugins: [ElementPlus],
        stubs: { DictTag: true }
      }
    })

    await flushPromises()

    expect(wrapper.text()).toContain("整改")
    expect(wrapper.text()).toContain("INV-2026-0007")
    expect(wrapper.text()).toContain("消防门闭合器损坏")
  })

  it("?????????????????????", async () => {
    routeState.path = "/asset/real-estate/detail/20001/disposal"
    routeState.fullPath = routeState.path

    const wrapper = mount(AssetRealEstateDetailPage, {
      global: {
        plugins: [ElementPlus],
        stubs: { DictTag: true }
      }
    })

    await flushPromises()

    expect(wrapper.text()).toContain("????")
    expect(wrapper.text()).toContain("DIS-2026-0003")

    const jumpButton = wrapper.get('[data-testid="disposal-jump-button"]')
    await jumpButton.trigger("click")

    expect(mockPush).toHaveBeenCalledWith({
      path: "/asset/disposal",
      query: {
        tab: "record",
        assetId: "20001",
        assetCode: "RE-2026-0001"
      }
    })
  })

  it("首屏参数延迟注入时也应补拉详情数据", async () => {
    routeState.params = {}
    routeState.path = "/asset/real-estate/detail/20001/inspection"
    routeState.fullPath = routeState.path

    const wrapper = mount(AssetRealEstateDetailPage, {
      global: {
        plugins: [ElementPlus],
        stubs: { DictTag: true }
      }
    })

    await flushPromises()
    expect(wrapper.text()).toContain("暂无巡检记录")

    routeState.params = { assetId: "20001" }
    await flushPromises()

    expect(wrapper.text()).toContain("INV-2026-0007")
    expect(wrapper.text()).toContain("发起整改")
  })
})
