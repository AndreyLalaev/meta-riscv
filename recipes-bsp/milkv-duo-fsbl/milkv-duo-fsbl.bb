DESCRIPTION = "FSBL contains OpenSBI and u-boot binaries for Milk-V Duo"
LICENSE = "CLOSED"

inherit nopackages deploy

SRC_URI = " \
           git://github.com/milkv-duo/duo-buildroot-sdk-v2;protocol=https;branch=main;subpath=fsbl \
           file://0001-cpu-riscv-do-not-use-vendor-specific-extension.patch \
           file://mmap_conv.py \
           file://memmap.py \
          "
SRCREV = "6f8962c394dd0a05729abb089f0feb7d5cc4aa5e"

COMPATIBLE_MACHINE = "milkv-(duo|duo256m|duos)"

S = "${UNPACKDIR}/fsbl"
B = "${S}/build"

EXTRA_OEMAKE = " \
  CFLAGS=-Wno-error \
  LDFLAGS=--no-fatal-warnings \
"

TARGET_LDFLAGS = ""
SECURITY_LDFLAGS = ""

do_compile[depends] += "opensbi:do_deploy virtual/bootloader:do_deploy"

python () {
    if d.getVar('MACHINE') == 'milkv-duo':
        d.setVar('CHIP_ARCH', 'cv180x')
        d.setVar('DDR_CFG', 'ddr2_1333_x16')
    else:
        d.setVar('CHIP_ARCH', 'cv181x')
        d.setVar('DDR_CFG', 'ddr3_1866_x16')
}

DEFINES  = " \
            -DBOARD_${@'${MACHINE}'.upper().replace('-', '_')} \
            -DRTOS_DUMP_PRINT_ENABLE=1 \
            -DRTOS_DUMP_PRINT_SZ_IDX=17 \
            -DRTOS_ENABLE_FREERTOS=y \
            -DRTOS_FAST_IMAGE_TYPE=0 \
           "

do_configure:append () {
    python3 ${UNPACKDIR}/mmap_conv.py --type h \
        ${UNPACKDIR}/memmap.py \
        ${S}/include/cvi_board_memmap.h
}

do_compile () {
    # this is a risc-v bin that contains a busy loop instruction
    # using wfi instruction, this is needed to initialize the
    # secondary core.

    printf '\163\000\120\020\157\360\337\377' > ${B}/blank.bin

    unset LDFLAGS

    export DEFINES='${DEFINES}'
    export ARCH=riscv
    export BOOT_CPU=riscv
    export CHIP_ARCH=${CHIP_ARCH}
    export DDR_CFG=${DDR_CFG}

    oe_runmake -C ${S} \
        CROSS_COMPILE=${HOST_PREFIX} \
        BLCP_2ND_PATH=${B}/blank.bin \
        LOADER_2ND_PATH=${DEPLOY_DIR_IMAGE}/u-boot.bin \
        MONITOR_PATH=${DEPLOY_DIR_IMAGE}/fw_dynamic.bin
}

do_deploy () {
    install -m 0644 ${B}/${CHIP_ARCH}/fip.bin ${DEPLOYDIR}
}

addtask deploy after do_compile
