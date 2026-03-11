import request from '@/utils/http';

export function listOrganization(query?: any) {
  return request.get<any>({
    url: '/hr/organization/list',
    params: query,
    returnFullResponse: true
  });
}

export function getOrganization(orgId: number | string) {
  return request.get<any>({
    url: '/hr/organization/' + orgId,
    returnFullResponse: true
  });
}

export function treeselect() {
  return request.get<any>({
    url: '/hr/organization/treeselect',
    returnFullResponse: true
  });
}

export function addOrganization(data: any) {
  return request.post<any>({
    url: '/hr/organization',
    data: data,
    returnFullResponse: true
  });
}

export function updateOrganization(data: any) {
  return request.put<any>({
    url: '/hr/organization',
    data: data,
    returnFullResponse: true
  });
}

export function delOrganization(orgId: number | string) {
  return request.del<any>({
    url: '/hr/organization/' + orgId,
    returnFullResponse: true
  });
}
