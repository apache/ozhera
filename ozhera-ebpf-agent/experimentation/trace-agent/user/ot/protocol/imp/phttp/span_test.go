package phttp

import (
	"context"
	"go.opentelemetry.io/otel/propagation"
	"go.opentelemetry.io/otel/trace"
	"testing"
)

func TestHeaders(t *testing.T) {
	h, _ := getHeaders([]byte("HTTP/1.1 200 OK\r\nAccept-Ranges: bytes\r\nCache-Control: private, no-cache, no-store, proxy-revalidate, no-transform\r\nConnection: keep-alive\r\nContent-Length: 2381\r\nContent-Type: text/html\r\nDate: Wed, 27 Sep 2023 03:06:32 GMT\r\ntraceparent: 00-bf52a8a6058547154e01a428d4dc5fed-42a575255e184859-01\r\nLast-Modified: Mon, 23 Jan 2017 13:27:43 GMT\r\nPragma: no-cache\r\nServer: bfe/1.0.8.18\r\nSet-Cookie: BDORZ=27315; max-age=86400; domain=.baidu.com; path=/\r\n\r\n<!DOCTYPE html>\r\n<!--STATUS OK--><html> <head><meta http-equiv=content-type content=text/html;charset=utf-8><meta http-equiv=X-UA-Compatible content=IE=Edge><meta content=always name=referrer><link rel=stylesheet type=text/css href=http://s1.bdstatic.com/r/www/cache/bdorz/baidu.min.css><title>百度一下，你就知道</title></head> <body link=#0000cc> <div id=wrapper> <div id=head> <div class=head_wrapper> <div class=s_form> <div class=s_form_wrapper> <div id=lg> <img hidefocus=true src=//www.baidu.com/img/bd_logo1.png width=270 height=129> </div> <form id=form name=f action=//www.baidu.com/s class=fm> <input type=hidden name=bdorz_come value=1> <input type=hidden name=ie value=utf-8> <input type=hidden name=f value=8> <input type=hidden name=rsv_bp value=1> <input type=hidden name=rsv_idx value=1> <input type=hidden name=tn value=baidu><span class=\"bg s_ipt_wr\"><input id=kw name=wd class=s_ipt value maxlength=255 autocomplete=off autofocus>"))
	propgator := propagation.TraceContext{} //(propagation.TraceContext{}, propagation.Baggage{})
	ctx := propgator.Extract(context.Background(), propagation.HeaderCarrier(h))
	p := trace.SpanFromContext(ctx)
	p.SpanContext()
	p.End()
	t.Log(p)
}
func TestNum(t *testing.T) {
	t.Log((512 << 20) / 1000 / 1000)
}
