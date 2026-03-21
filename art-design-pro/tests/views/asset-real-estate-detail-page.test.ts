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
            taskId: 6,
            taskNo: "INV-2026-0008",
            taskName: "????????",
            inventoryResult: "LOCATION_DIFF",
            followUpAction: "UPDATE_LEDGER",
            checkedBy: "?????",
            checkedTime: "2026-03-01 09:30:00"
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
    expect(wrapper.text()).toContain("???????????")
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
})
