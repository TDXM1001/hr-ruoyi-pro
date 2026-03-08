import { ref, toRefs } from 'vue'
import { useDictStore } from '@/store/modules/dict'
import { getDicts } from '@/api/system/dict/data'

/**
 * 获取字典数据
 */
export function useDict(...args: string[]) {
  const res = ref<any>({})

  args.forEach((dictType) => {
    // 关键修复：必须先初始化键，否则 toRefs 返回的引用是非响应式的
    res.value[dictType] = []

    const dicts = useDictStore().getDict(dictType)
    if (dicts && dicts.length > 0) {
      res.value[dictType] = dicts
    } else {
      getDicts(dictType).then((resp: any) => {
        // 适配若依后端：优先取 data，否则取 rows
        const rawData = resp?.data || resp?.rows || resp || []

        if (Array.isArray(rawData)) {
          const mappedData = rawData.map((item: any) => ({
            ...item,
            label: item.dictLabel || item.label,
            value: item.dictValue || item.value,
            elTagType: item.listClass || 'primary',
            elTagClass: item.cssClass
          }))
          res.value[dictType] = mappedData
          useDictStore().setDict(dictType, mappedData)
        }
      })
    }
  })

  return toRefs(res.value)
}
