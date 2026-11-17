.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    x_main DD ?
    y_main DD ?
    w_main DD ?
    z_main DQ ?
    c_2_ul_ul DD 2
    c_4_ul_ul DD 4
    c_6_ul_ul DD 6
    p1_main DD ?
    p2_main DD ?
    a_main_base DD ?
    pa_main_base_m DD ?
    b_main_hija DD ?
    n_main_foo DD ?
    d_main_foo DQ ?
    c_0_ul_ul DD 0
    c_4294967295_ul_ul DD 4294967295
    c_1_0 DQ 1.0
    c_6d_neg_1 DQ .6d-1
    c_1_ul_ul DD 1
    ok DB "ok",0
    c_2_5 DQ 2.5
    c_3_ul_ul DD 3
    hola DB "hola",0
    @aux16 DD ?
    @aux18 DD ?
    @aux19 DD ?
    @aux20 DD ?
    @aux21 DD ?
    @aux23 DD ?
    @aux24 DD ?
    @aux28 DQ ?
    @aux32 DD ?
    @aux36 DD ?
    @aux38 DD ?
    @aux42 DD ?

.code
start:
    ; TODO: traducir tercetos a instrucciones MASM32 (Prompt 3+)
    invoke ExitProcess, 0
end start
