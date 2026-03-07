import { ref, toRefs } from 'vue'
import { useDictStore } from '@/store/modules/dict'
import { getDicts } from '@/api/system/dict/data'

/**
 * 获取字典数据
 */
export function useDict(...args: string[]) {
  const res = ref<{ [key: string]: any[] }>({})
  return (() => {
    args.forEach((dictType) => {
      res.value[dictType] = []
      const dicts = useDictStore().getDict(dictType)
      if (dicts) {
        res.value[dictType] = dicts
      } else {
        getDicts(dictType).then((resp) => {
          // 将响应结果强制转为数组，防止 TS 推断类型不匹配
          res.value[dictType] = (resp as any[]) || []
          useDictStore().setDict(dictType, res.value[dictType])
        })
      }
    })
    return toRefs(res.value)
  })()
}
